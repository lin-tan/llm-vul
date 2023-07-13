protected void innerAcquireNewsThroughNumber(AsyncResponse asyncReply, long recordsNumber, long itemNumber,
                                          boolean reliable) {
    try {
        confirmSubjectPossession(subjectTitle, reliable);
        confirmSubjectAction(subjectTitle, SubjectAction.PEEK_MESSAGES);
        if (subjectTitle.whetherUniversal()) {
            confirmUniversalTitleScopePossession(titleScopeTitle);
        }
        TenaciousSubject subject = (TenaciousSubject) acquireSubjectMention(subjectTitle);
        AdministeredRecordsImpl records = (AdministeredRecordsImpl) subject.acquireAdministeredRecords();
        records.asyncRetrieveItem(new PlaceImpl(recordsNumber, itemNumber), new AsyncCallbacks.ReadEntryCallback() {
            @Override
            public void retrieveItemUnsuccess(AdministeredRecordsException exception, Object obj) {
                asyncReply.resume(new RestException(exception));
            }
            @Override
            public void retrieveItemFinish(Item item, Object obj) {
                try {
                    asyncReply.resume(produceReplyWithItem(item));
                } catch (IOException exception) {
                    asyncReply.resume(new RestException(exception));
                } finally {
                    if (item != null) {
                        item.free();
                    }
                }
            }
        }, null);
    } catch (NullPointerException nullPointerException) {
        asyncReply.resume(new RestException(Status.NOT_FOUND, "Message not found"));
    } catch (Exception exception) {
        log.error("[{}] Failed to get message with ledgerId {} entryId {} from {}",
                customerAppNumber(), recordsNumber, itemNumber, subjectTitle, exception);
        asyncReply.resume(new RestException(exception));
    }
}