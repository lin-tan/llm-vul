@SuppressWarnings({"unchecked", "rawtypes"}) 
@Override
public boolean start() throws Exception {
    String job = step.getJob();
    Item item = Jenkins.getActiveInstance().getItem(job, invokingRun.getParent(), Item.class);
    if (item != null) {
        if ( !step.getWait() || (item instanceof Job)) {
            if(!(item instanceof ParameterizedJobMixIn.ParameterizedJob)){
                if(!(item instanceof Queue.Task)){
                    String s;
                    if(item instanceof Describable){
                        s =  ((Describable) item).getDescriptor().getDisplayName();
                    }else{
                        s =  item.getClass().getName();
                    }
                    throw new AbortException("The item named " + job + " is a " + s  + " which is not something that can be built");
                }else{
                    if (step.getParameters() == null || step.getParameters().isEmpty()) {
                        Queue.Task task = (Queue.Task) item;
                        String itemEncoded = ModelHyperlinkNote.encodeTo(item);
                        listener.getLogger().println("Scheduling item: " +itemEncoded );
                        String taskName = task.getFullDisplayName();
                        String name  = Messages.BuildTriggerStepExecution_building_(taskName);
                        LabelAction labelAction = new LabelAction(name);
                        node.addAction(labelAction);
                        List<Action> actions = new ArrayList<>();
                        if (step.getWait()) {
                            StepContext context = getContext();
                            boolean isPropagate =  step.isPropagate();
                            actions.add(new BuildTriggerAction(context, isPropagate ));
                            LOGGER.log(Level.FINER, "scheduling a build of {0} from {1}", new Object[]{task, context});
                        }
                        Cause.UpstreamCause cause = new Cause.UpstreamCause(invokingRun);
                        CauseAction causeAction = new CauseAction(cause);
                        actions.add( causeAction);
                        Integer quietPeriod = step.getQuietPeriod();
                        if (quietPeriod == null) {
                            try {
                                Class taskClass = task.getClass();
                                Method getQuietPeriod = taskClass.getMethod("getQuietPeriod");
                                Class returnType  = getQuietPeriod.getReturnType();
                                if (returnType.equals(int.class)) {
                                    quietPeriod = (Integer) getQuietPeriod.invoke(task);
                                }
                            } catch (NoSuchMethodException e) {
                            } catch (IllegalAccessError | IllegalArgumentException | InvocationTargetException e) {
                                LOGGER.log(Level.WARNING, "Could not determine quiet period of " + item.getFullName(), e);
                            }
                        }
                        quietPeriod = quietPeriod == null? Jenkins.getActiveInstance().getQuietPeriod():quietPeriod;
                        Jenkins activeInstance = Jenkins.getActiveInstance();
                        ScheduleResult scheduleResult = activeInstance.getQueue().schedule2(task, quietPeriod,actions);
                        if (scheduleResult.isRefused()) {
                            throw new AbortException("Failed to trigger build of " + item.getFullName());
                        }

                    }else{
                        throw new AbortException("Item type does not support parameters");
                    }
                }
            }else{
                final ParameterizedJobMixIn.ParameterizedJob project = (ParameterizedJobMixIn.ParameterizedJob) item;
                listener.getLogger().println("Scheduling project: " + ModelHyperlinkNote.encodeTo(project));

                node.addAction(new LabelAction(Messages.BuildTriggerStepExecution_building_(project.getFullDisplayName())));
                List<Action> actions = new ArrayList<>();
                if (step.getWait()) {
                    StepContext context = getContext();
                    boolean isPropagate =  step.isPropagate();
                    actions.add(new BuildTriggerAction(context,isPropagate ));
                    LOGGER.log(Level.FINER, "scheduling a build of {0} from {1}", new Object[]{project, context});
                }

                Cause.UpstreamCause cause = new Cause.UpstreamCause(invokingRun);
                CauseAction causeAction = new CauseAction(cause);
                actions.add(causeAction);
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
            } 

            if (!step.getWait()) {
                getContext().onSuccess(null);
                return true;
            } else {
                return false;
            }

            
        }else{
            throw new AbortException("Waiting for non-job items is not supported");
        }
            
    }else{
        throw new AbortException("No item named " + job + " found");
    }

}
