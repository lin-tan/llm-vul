@SuppressWarnings({ "unchecked", "rawtypes" })
public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
    final JSONLexer lexer = parser.lexer;
    if (lexer.token() == JSONToken.NULL) {
        lexer.nextToken(JSONToken.COMMA);
        return null;
    }
    if (lexer.token() == JSONToken.LITERAL_STRING) {
        byte[] bytes = lexer.bytesValue();
        lexer.nextToken(JSONToken.COMMA);
        return (T) bytes;
    }
    Class componentClass;
    Type componentType;
    if (type instanceof GenericArrayType) {
        GenericArrayType clazz = (GenericArrayType) type;
        componentType = clazz.getGenericComponentType();
        if (componentType instanceof TypeVariable) {
            TypeVariable typeVar = (TypeVariable) componentType;
            Type objType = parser.getContext().type;
            if (objType instanceof ParameterizedType) {
                ParameterizedType objParamType = (ParameterizedType) objType;
                Type objRawType = objParamType.getRawType();
                Type actualType = null;
                if (objRawType instanceof Class) {
                    TypeVariable[] objTypeParams = ((Class) objRawType).getTypeParameters();
                    for (int i = 0; i < objTypeParams.length; ++i) {
                        if (objTypeParams[i].getName().equals(typeVar.getName())) {
                            actualType = objParamType.getActualTypeArguments()[i];
                        }
                    }
                }
                if (actualType instanceof Class) {
                    componentClass = (Class) actualType;
                } else {
                    componentClass = Object.class;
                }
            } else {
                componentClass = TypeUtils.getClass(typeVar.getBounds()[0]);
            }
        } else {
            componentClass = TypeUtils.getClass(componentType);
        }
    } else {
        Class clazz = (Class) type;
        componentType = componentClass = clazz.getComponentType();
    }
    JSONArray array = new JSONArray();
    parser.parseArray(componentClass, array, fieldName);

    return (T) toObjectArray(parser, componentClass, array);
}
