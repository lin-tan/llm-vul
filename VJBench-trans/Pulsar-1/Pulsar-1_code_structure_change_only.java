
    protected void internalGetMessageById(AsyncResponse asyncResponse, long ledgerId, long entryId,
                                              boolean authoritative) {
        try {
            validateTopicOwnership(topicName, authoritative);
            validateTopicOperation(topicName, TopicOperation.PEEK_MESSAGES);

            if (topicName.isGlobal()) {
                validateGlobalNamespaceOwnership(namespaceName);
            }

            AsyncCallbacks.ReadEntryCallback readEntryCallback = new AsyncCallbacks.ReadEntryCallback() {

                @Override
                public void readEntryComplete(Entry entry, Object ctx) {
                    try {
                        Response response = generateResponseWithEntry(entry);
                        asyncResponse.resume(response);
                    } catch (IOException exception) {
                        RestException restException =  new RestException(exception);
                        asyncResponse.resume(restException);
                    } finally {
                        if (entry == null) {
                            return;
                        }else{
                            entry.release();
                        }
                    }
                }

                @Override
                public void readEntryFailed(ManagedLedgerException exception, Object ctx) {
                    RestException restException =  new RestException(exception);
                    asyncResponse.resume(restException);
                }

            };

            PositionImpl positionImpl = new PositionImpl(ledgerId, entryId);
            ((ManagedLedgerImpl) ((PersistentTopic) getTopicReference(topicName)).getManagedLedger())
                .asyncReadEntry(positionImpl,readEntryCallback , null);
        } catch (NullPointerException npe) {
            RestException restException =  new RestException(Status.NOT_FOUND, "Message not found");
            asyncResponse.resume(restException);
        } catch (Exception exception) {
            String id = clientAppId();
            log.error("[{}] Failed to get message with ledgerId {} entryId {} from {}",
                    id , ledgerId, entryId, topicName, exception);
            RestException restException =  new RestException(exception);
            asyncResponse.resume(restException);
        }
    }
