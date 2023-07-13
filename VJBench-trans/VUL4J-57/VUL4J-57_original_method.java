@SuppressWarnings({"unchecked", "rawtypes"}) 
@Override
public boolean start() throws Exception {
    String job = step.getJob();
    Item item = Jenkins.getActiveInstance().getItem(job, invokingRun.getParent(), Item.class);
    if (item == null) {
        throw new AbortException("No item named " + job + " found");
    }
    if (step.getWait() && !(item instanceof Job)) {
        
        throw new AbortException("Waiting for non-job items is not supported");
    }
    if (item instanceof ParameterizedJobMixIn.ParameterizedJob) {
        final ParameterizedJobMixIn.ParameterizedJob project = (ParameterizedJobMixIn.ParameterizedJob) item;
        listener.getLogger().println("Scheduling project: " + ModelHyperlinkNote.encodeTo(project));
        node.addAction(new LabelAction(Messages.BuildTriggerStepExecution_building_(project.getFullDisplayName())));
        List<Action> actions = new ArrayList<>();
        if (step.getWait()) {
            StepContext context = getContext();
            actions.add(new BuildTriggerAction(context, step.isPropagate()));
            LOGGER.log(Level.FINER, "scheduling a build of {0} from {1}", new Object[]{project, context});
        }
        actions.add(new CauseAction(new Cause.UpstreamCause(invokingRun)));
        List<ParameterValue> parameters = step.getParameters();
        if (parameters != null) {
            parameters = completeDefaultParameters(parameters, (Job) project);
            actions.add(new ParametersAction(parameters));
        }
        Integer quietPeriod = step.getQuietPeriod();
        if (quietPeriod == null) {
            quietPeriod = project.getQuietPeriod();
        }
        QueueTaskFuture<?> f = new ParameterizedJobMixIn() {
            @Override
            protected Job asJob() {
                return (Job) project;
            }
        }.scheduleBuild2(quietPeriod, actions.toArray(new Action[actions.size()]));
        if (f == null) {
            throw new AbortException("Failed to trigger build of " + project.getFullName());
        }
    } else if (item instanceof Queue.Task){
        if (step.getParameters() != null && !step.getParameters().isEmpty()) {
            throw new AbortException("Item type does not support parameters");
        }
        Queue.Task task = (Queue.Task) item;
        listener.getLogger().println("Scheduling item: " + ModelHyperlinkNote.encodeTo(item));
        node.addAction(new LabelAction(Messages.BuildTriggerStepExecution_building_(task.getFullDisplayName())));
        List<Action> actions = new ArrayList<>();
        if (step.getWait()) {
            StepContext context = getContext();
            actions.add(new BuildTriggerAction(context, step.isPropagate()));
            LOGGER.log(Level.FINER, "scheduling a build of {0} from {1}", new Object[]{task, context});
        }
        actions.add(new CauseAction(new Cause.UpstreamCause(invokingRun)));
        Integer quietPeriod = step.getQuietPeriod();
        if (quietPeriod == null) {
            try {
                Method getQuietPeriod = task.getClass().getMethod("getQuietPeriod");
                if (getQuietPeriod.getReturnType().equals(int.class)) {
                    quietPeriod = (Integer) getQuietPeriod.invoke(task);
                }
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessError | IllegalArgumentException | InvocationTargetException e) {
                LOGGER.log(Level.WARNING, "Could not determine quiet period of " + item.getFullName(), e);
            }
        }
        if (quietPeriod == null) {
            quietPeriod = Jenkins.getActiveInstance().getQuietPeriod();
        }
        ScheduleResult scheduleResult = Jenkins.getActiveInstance().getQueue().schedule2(task, quietPeriod,actions);
        if (scheduleResult.isRefused()) {
            throw new AbortException("Failed to trigger build of " + item.getFullName());
        }
    } else {
        throw new AbortException("The item named " + job + " is a "
                + (item instanceof Describable
                ? ((Describable) item).getDescriptor().getDisplayName()
                : item.getClass().getName())
                + " which is not something that can be built");
    }
    if (step.getWait()) {
        return false;
    } else {
        getContext().onSuccess(null);
        return true;
    }
}
