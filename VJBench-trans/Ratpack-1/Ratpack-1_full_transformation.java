 private void createQuery(ChannelHandlerContext ctx, HttpRequest httpQuery) throws Exception {
    DecoderResult decoderResult = httpQuery.decoderResult();
    if (decoderResult.isSuccess()) {
      HttpHeaders httpHeaders = httpQuery.headers();
      Headings queryHeading = new NetworkLibraryHeadingSupporteHeadings(httpHeaders);
      Long messageSize = HttpUtil.getContentLength(httpQuery, -1L);
      boolean withContent = (messageSize > 0) || (queryHeading.get(HttpHeaderNames.TRANSFER_ENCODING) != null);
      QueryContent queryContent;
      if(!withContent){
        queryContent =  null;
      }else{
        queryContent  = new QueryContent(messageSize, httpQuery, ctx);
      }
    
      Channel channel = ctx.channel();

      if (queryContent != null) {
        channel.attr(contentCollectorKey).set(queryContent);
      }

      X509Certificate clientCertificate = channel.attr(customerAutorizationKey).get();
      Instant instant = time.instant();
      PrimaryQuery query = new PrimaryQuery(
        instant,
        queryHeading,
        httpQuery.method(),
        httpQuery.protocolVersion(),
        httpQuery.uri(),
        (InetSocketAddress) channel.distantReference(),
        (InetSocketAddress) channel.localAddress(),
        hostRepository.get(ServerConfig.class),
        queryContent,
        ConnexionSpareTimeOver.about(channel),
        clientCertificate
      );

      HttpHeaders heading = new DefaultHttpHeaders(false);
      ChangeableHeadings replyHeading = new NetworkLibraryHeadingSupportedChangeableHeadings(heading);
      AtomicBoolean conveyed = new AtomicBoolean(false);

      PrimaryReplySender replySender = new PrimaryReplySender(conveyed, channel, time, httpQuery, query, heading, queryContent);

      Channel ctxChannel = ctx.channel();

      ctxChannel.attr(PrimaryReplySender.ATTRIBUTE_KEY).set(replySender);

      PrimaryCircumstance.QueryConstants queryConstants = new PrimaryCircumstance.QueryConstants(
        appConstants,
        query,
        channel,
        replySender,
        matter -> {
        conveyed.set(true);
        ctx.channel().attr(channelReceiverPropertyKey).set(matter);
      }
      );
      ByteBufAllocator byteBufAllocator =  ctx.alloc();
      Reply reply = new PrimaryReply(replyHeading, byteBufAllocator, replySender);
      queryConstants.reply = reply;
      EventLoop eventLoop = channel.eventLoop();
      PrimaryCircumstance.begin(eventLoop, queryConstants, hostRepository, managers, executing -> {
        if (!conveyed.get()) {
          Manager stoppingPointManager = queryConstants.manager;
          StringBuilder description = new StringBuilder();
          description.append("No response sent for ");
          String name = query.acquireFunction().acquireTitle();
          description.append(name);
          description.append(" request to ");
          String uri = query.acquireUri();
          description.append(uri);

          if (stoppingPointManager != null) {
            description.append(" (last handler: ");

            if (!(stoppingPointManager instanceof RecountingManager)) {
              RecountingManagers.depictTo(stoppingPointManager, description);
            } else {
              ((RecountingManager) stoppingPointManager).depictTo(description);
            }
            description.append(")");
          }

          String news = description.toString();
          LOGGER.warn(news);

          reply.acquireHeading().empty();

          ByteBuf content;
          if (!evolution) {
            content = Unpooled.EMPTY_BUFFER;
          } else {
            ByteBufAllocator byteBufAllocator2 =  ctx.alloc();
            CharBuffer charBuffer =  CharBuffer.wrap(news);
            content = ByteBufUtil.encodeString(byteBufAllocator2, charBuffer, CharsetUtil.UTF_8);
            reply.messageKind(HypertextTransferProtocolHeadingConstants.PLAIN_TEXT_UTF8);
          }
          int num = content.readableBytes();
          reply.acquireHeading().set(HypertextTransferProtocolHeadingConstants.MESSAGE_SIZE, num);
          replySender.convey(HttpResponseStatus.INTERNAL_SERVER_ERROR, content);
        }
      });
   
    }else{
      LOGGER.debug("Failed to decode HTTP request.", decoderResult.cause());
      directMistake(ctx, HttpResponseStatus.BAD_REQUEST);
      return;
    }

  }