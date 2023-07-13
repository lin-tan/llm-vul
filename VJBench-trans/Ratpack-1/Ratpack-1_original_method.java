private void newRequest(ChannelHandlerContext ctx, HttpRequest nettyRequest) throws Exception {
  if (!nettyRequest.decoderResult().isSuccess()) {
    LOGGER.debug("Failed to decode HTTP request.", nettyRequest.decoderResult().cause());
    sendError(ctx, HttpResponseStatus.BAD_REQUEST);
    return;
  }
  Headers requestHeaders = new NettyHeadersBackedHeaders(nettyRequest.headers());
  Long contentLength = HttpUtil.getContentLength(nettyRequest, -1L);
  String transferEncoding = requestHeaders.get(HttpHeaderNames.TRANSFER_ENCODING);
  boolean hasBody = (contentLength > 0) || (transferEncoding != null);
  RequestBody requestBody = hasBody ? new RequestBody(contentLength, nettyRequest, ctx) : null;
  Channel channel = ctx.channel();
  if (requestBody != null) {
    channel.attr(BODY_ACCUMULATOR_KEY).set(requestBody);
  }
  InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
  InetSocketAddress socketAddress = (InetSocketAddress) channel.localAddress();
  ConnectionIdleTimeout connectionIdleTimeout = ConnectionIdleTimeout.of(channel);
  DefaultRequest request = new DefaultRequest(
    clock.instant(),
    requestHeaders,
    nettyRequest.method(),
    nettyRequest.protocolVersion(),
    nettyRequest.uri(),
    remoteAddress,
    socketAddress,
    serverRegistry.get(ServerConfig.class),
    requestBody,
    connectionIdleTimeout,
    channel.attr(CLIENT_CERT_KEY).get()
  );
  HttpHeaders nettyHeaders = new DefaultHttpHeaders(false);
  MutableHeaders responseHeaders = new NettyHeadersBackedMutableHeaders(nettyHeaders);
  AtomicBoolean transmitted = new AtomicBoolean(false);
  DefaultResponseTransmitter responseTransmitter = new DefaultResponseTransmitter(transmitted, channel, clock, nettyRequest, request, nettyHeaders, requestBody);
  ctx.channel().attr(DefaultResponseTransmitter.ATTRIBUTE_KEY).set(responseTransmitter);
  Action<Action<Object>> subscribeHandler = thing -> {
    transmitted.set(true);
    ctx.channel().attr(CHANNEL_SUBSCRIBER_ATTRIBUTE_KEY).set(thing);
  };
  DefaultContext.RequestConstants requestConstants = new DefaultContext.RequestConstants(
    applicationConstants,
    request,
    channel,
    responseTransmitter,
    subscribeHandler
  );
  Response response = new DefaultResponse(responseHeaders, ctx.alloc(), responseTransmitter);
  requestConstants.response = response;
  DefaultContext.start(channel.eventLoop(), requestConstants, serverRegistry, handlers, execution -> {
    if (!transmitted.get()) {
      Handler lastHandler = requestConstants.handler;
      StringBuilder description = new StringBuilder();
      description
        .append("No response sent for ")
        .append(request.getMethod().getName())
        .append(" request to ")
        .append(request.getUri());
      if (lastHandler != null) {
        description.append(" (last handler: ");
        if (lastHandler instanceof DescribingHandler) {
          ((DescribingHandler) lastHandler).describeTo(description);
        } else {
          DescribingHandlers.describeTo(lastHandler, description);
        }
        description.append(")");
      }
      String message = description.toString();
      LOGGER.warn(message);
      response.getHeaders().clear();
      ByteBuf body;
      if (development) {
        CharBuffer charBuffer = CharBuffer.wrap(message);
        body = ByteBufUtil.encodeString(ctx.alloc(), charBuffer, CharsetUtil.UTF_8);
        response.contentType(HttpHeaderConstants.PLAIN_TEXT_UTF8);
      } else {
        body = Unpooled.EMPTY_BUFFER;
      }
      response.getHeaders().set(HttpHeaderConstants.CONTENT_LENGTH, body.readableBytes());
      responseTransmitter.transmit(HttpResponseStatus.INTERNAL_SERVER_ERROR, body);
    }
  });
}
