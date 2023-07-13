
    protected void innerAcquireNewsThroughNumber(AsyncResponse asyncReply, long recordsNumber, long itemNumber,
                                              boolean reliable) {
        try {
            confirmSubjectPossession(subjectTitle, reliable);
            confirmSubjectAction(subjectTitle, SubjectAction.PEEK_MESSAGES);

            if (subjectTitle.whetherUniversal()) {
                confirmUniversalTitleScopePossession(titleScopeTitle);
            }

            AsyncCallbacks.ReadEntryCallback readEntryCallback = new AsyncCallbacks.ReadEntryCallback() {

                @Override
                public void retrieveItemFinish(Item item, Object obj) {
                    try {
                        Response response = produceReplyWithItem(item);
                        asyncReply.resume(response);
                    } catch (IOException exception) {
                        RestException restException =  new RestException(exception);
                        asyncReply.resume(restException);
                    } finally {
                        if (item == null) {
                            return;
                        }else{
                            item.free();
                        }
                    }
                }

                @Override
                public void retrieveItemUnsuccess(AdministeredRecordsException exception, Object obj) {
                    RestException restException =  new RestException(exception);
                    asyncReply.resume(restException);
                }

            };

            PlaceImpl positionImpl = new PlaceImpl(recordsNumber, itemNumber);
            ((AdministeredRecordsImpl) ((TenaciousSubject) acquireSubjectMention(subjectTitle)).acquireAdministeredRecords())
                .asyncRetrieveItem(positionImpl,readEntryCallback , null);
        } catch (NullPointerException nullPointerException) {
            RestException restException =  new RestException(Status.NOT_FOUND, "Message not found");
            asyncReply.resume(restException);
        } catch (Exception exception) {
            String id = customerAppNumber();
            log.error("[{}] Failed to get message with ledgerId {} entryId {} from {}",
                    id , recordsNumber, itemNumber, subjectTitle, exception);
            RestException restException =  new RestException(exception);
            asyncReply.resume(restException);
        }
    }