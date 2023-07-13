 private void newRequest(ChannelHandlerContext ctx, HttpRequest nettyRequest) throws Exception {
    DecoderResult decoderResult = nettyRequest.decoderResult();
    if (decoderResult.isSuccess()) {
      HttpHeaders httpHeaders = nettyRequest.headers();
      Headers requestHeaders = new NettyHeadersBackedHeaders(httpHeaders);
      Long contentLength = HttpUtil.getContentLength(nettyRequest, -1L);
      boolean hasBody = (contentLength > 0) || (requestHeaders.get(HttpHeaderNames.TRANSFER_ENCODING) != null);
      RequestBody requestBody;
      if(!hasBody){
        requestBody =  null;
      }else{
        requestBody  = new RequestBody(contentLength, nettyRequest, ctx);
      }
    
      Channel channel = ctx.channel();

      if (requestBody != null) {
        channel.attr(BODY_ACCUMULATOR_KEY).set(requestBody);
      }

      X509Certificate clientCertificate = channel.attr(CLIENT_CERT_KEY).get();
      Instant instant = clock.instant();
      DefaultRequest request = new DefaultRequest(
        instant,
        requestHeaders,
        nettyRequest.method(),
        nettyRequest.protocolVersion(),
        nettyRequest.uri(),
        (InetSocketAddress) channel.remoteAddress(),
        (InetSocketAddress) channel.localAddress(),
        serverRegistry.get(ServerConfig.class),
        requestBody,
        ConnectionIdleTimeout.of(channel),
        clientCertificate
      );

      HttpHeaders nettyHeaders = new DefaultHttpHeaders();
      MutableHeaders responseHeaders = new NettyHeadersBackedMutableHeaders(nettyHeaders);
      AtomicBoolean transmitted = new AtomicBoolean(false);

      DefaultResponseTransmitter responseTransmitter = new DefaultResponseTransmitter(transmitted, channel, clock, nettyRequest, request, nettyHeaders, requestBody);

      Channel ctxChannel = ctx.channel();

      ctxChannel.attr(DefaultResponseTransmitter.ATTRIBUTE_KEY).set(responseTransmitter);

      DefaultContext.RequestConstants requestConstants = new DefaultContext.RequestConstants(
        applicationConstants,
        request,
        channel,
        responseTransmitter,
        thing -> {
        transmitted.set(true);
        ctx.channel().attr(CHANNEL_SUBSCRIBER_ATTRIBUTE_KEY).set(thing);
      }
      );
      ByteBufAllocator byteBufAllocator =  ctx.alloc();
      Response response = new DefaultResponse(responseHeaders, byteBufAllocator, responseTransmitter);
      requestConstants.response = response;
      EventLoop eventLoop = channel.eventLoop();
      DefaultContext.start(eventLoop, requestConstants, serverRegistry, handlers, execution -> {
        if (!transmitted.get()) {
          Handler lastHandler = requestConstants.handler;
          StringBuilder description = new StringBuilder();
          description.append("No response sent for ");
          String name = request.getMethod().getName();
          description.append(name);
          description.append(" request to ");
          String uri = request.getUri();
          description.append(uri);

          if (lastHandler != null) {
            description.append(" (last handler: ");

            if (!(lastHandler instanceof DescribingHandler)) {
              DescribingHandlers.describeTo(lastHandler, description);
            } else {
              ((DescribingHandler) lastHandler).describeTo(description);
            }
            description.append(")");
          }

          String message = description.toString();
          LOGGER.warn(message);

          response.getHeaders().clear();

          ByteBuf body;
          if (!development) {
            body = Unpooled.EMPTY_BUFFER;
          } else {
            ByteBufAllocator byteBufAllocator2 =  ctx.alloc();
            CharBuffer charBuffer =  CharBuffer.wrap(message);
            body = ByteBufUtil.encodeString(byteBufAllocator2, charBuffer, CharsetUtil.UTF_8);
            response.contentType(HttpHeaderConstants.PLAIN_TEXT_UTF8);
          }
          int num = body.readableBytes();
          response.getHeaders().set(HttpHeaderConstants.CONTENT_LENGTH, num);
          responseTransmitter.transmit(HttpResponseStatus.INTERNAL_SERVER_ERROR, body);
        }
      });
   
    }else{
      LOGGER.debug("Failed to decode HTTP request.", decoderResult.cause());
      sendError(ctx, HttpResponseStatus.BAD_REQUEST);
      return;
    }

  }