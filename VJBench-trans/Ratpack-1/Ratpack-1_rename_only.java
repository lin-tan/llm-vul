private void createQuery(ChannelHandlerContext context, HttpRequest httpQuery) throws Exception {
  if (!httpQuery.decoderResult().isSuccess()) {
    LOGGER.debug("Failed to decode HTTP request.", httpQuery.decoderResult().cause());
    directMistake(context, HttpResponseStatus.BAD_REQUEST);
    return;
  }
  Headings queryHeading = new NetworkLibraryHeadingSupporteHeadings(httpQuery.headers());
  Long messageSize = HttpUtil.getContentLength(httpQuery, -1L);
  String transportationEncryption = queryHeading.get(HttpHeaderNames.TRANSFER_ENCODING);
  boolean withContent = (messageSize > 0) || (transportationEncryption != null);
  QueryContent queryContent = withContent ? new QueryContent(messageSize, httpQuery, context) : null;
  Channel channel = context.channel();
  if (queryContent != null) {
    channel.attr(contentCollectorKey).set(queryContent);
  }
  InetSocketAddress distantReference = (InetSocketAddress) channel.distantReference();
  InetSocketAddress reference = (InetSocketAddress) channel.localAddress();
  ConnexionSpareTimeOver connexionTickOver = ConnexionSpareTimeOver.about(channel);
  PrimaryQuery query = new PrimaryQuery(
    time.instant(),
    queryHeading,
    httpQuery.method(),
    httpQuery.protocolVersion(),
    httpQuery.uri(),
    distantReference,
    reference,
    hostRepository.get(ServerConfig.class),
    queryContent,
    connexionTickOver,
    channel.attr(customerAutorizationKey).get()
  );
  HttpHeaders heading = new DefaultHttpHeaders(false);
  ChangeableHeadings replyHeading = new NetworkLibraryHeadingSupportedChangeableHeadings(heading);
  AtomicBoolean conveyed = new AtomicBoolean(false);
  PrimaryReplySender replySender = new PrimaryReplySender(conveyed, channel, time, httpQuery, query, heading, queryContent);
  context.channel().attr(PrimaryReplySender.ATTRIBUTE_KEY).set(replySender);
  Operation<Operation<Object>> registerManager = matter -> {
    conveyed.set(true);
    context.channel().attr(channelReceiverPropertyKey).set(matter);
  };
  PrimaryCircumstance.QueryConstants queryConstants = new PrimaryCircumstance.QueryConstants(
    appConstants,
    query,
    channel,
    replySender,
    registerManager
  );
  Reply reply = new PrimaryReply(replyHeading, context.alloc(), replySender);
  queryConstants.reply = reply;
  PrimaryCircumstance.begin(channel.eventLoop(), queryConstants, hostRepository, managers, executing -> {
    if (!conveyed.get()) {
      Manager stoppingPointManager = queryConstants.manager;
      StringBuilder description = new StringBuilder();
      description
        .append("No response sent for ")
        .append(query.acquireFunction().acquireTitle())
        .append(" request to ")
        .append(query.acquireUri());
      if (stoppingPointManager != null) {
        description.append(" (last handler: ");
        if (stoppingPointManager instanceof RecountingManager) {
          ((RecountingManager) stoppingPointManager).depictTo(description);
        } else {
          RecountingManagers.depictTo(stoppingPointManager, description);
        }
        description.append(")");
      }
      String news = description.toString();
      LOGGER.warn(news);
      reply.acquireHeading().empty();
      ByteBuf content;
      if (evolution) {
        CharBuffer charBuffer = CharBuffer.wrap(news);
        content = ByteBufUtil.encodeString(context.alloc(), charBuffer, CharsetUtil.UTF_8);
        reply.messageKind(HypertextTransferProtocolHeadingConstants.PLAIN_TEXT_UTF8);
      } else {
        content = Unpooled.EMPTY_BUFFER;
      }
      reply.acquireHeading().set(HypertextTransferProtocolHeadingConstants.MESSAGE_SIZE, content.readableBytes());
      replySender.convey(HttpResponseStatus.INTERNAL_SERVER_ERROR, content);
    }
  });
}