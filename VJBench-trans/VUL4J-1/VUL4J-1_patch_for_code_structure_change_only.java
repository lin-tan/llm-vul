    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        switch (lexer.token()) {
            case JSONToken.LITERAL_STRING:
                byte[] bytes = lexer.bytesValue();
                lexer.nextToken(JSONToken.COMMA);
                return (T) bytes;
            case JSONToken.NULL:
                lexer.nextToken(JSONToken.COMMA);
                return null;
            default:
                break;
        }

        JSONArray array = new JSONArray();
        Class componentClass;
        Type componentType;
        if (!(type instanceof GenericArrayType)) {
            componentType =  ((Class) type).getComponentType();
            componentClass = ((Class) type).getComponentType();
        } else {
            componentType = ((GenericArrayType) type).getGenericComponentType();
            if (! (componentType instanceof TypeVariable)) {
                componentClass = TypeUtils.getClass(componentType);
            } else {
                   
                TypeVariable typeVar = (TypeVariable) componentType;
                Type objType = parser.getContext().type;
                if (!(objType instanceof ParameterizedType)) {
                    Type type_ = typeVar.getBounds()[0];
                    componentClass = TypeUtils.getClass(type_);
              
                } else {
          
                    ParameterizedType objParamType = (ParameterizedType) objType;
                    Type objRawType = objParamType.getRawType();
                    Type actualType = null;
                    if (objRawType instanceof Class) {
                        TypeVariable[] objTypeParams = ((Class) objRawType).getTypeParameters();
                        int i = 0;
                        while( i < objTypeParams.length) {
                            String i_name = objTypeParams[i].getName();
                            String typeVar_name = typeVar.getName();
                            if (!i_name.equals(typeVar_name)) {
                                i++;
                               continue;
                            }
                            actualType = objParamType.getActualTypeArguments()[i];
                            i++;
                        }
                    }
                    
                    componentClass = (actualType instanceof Class)? (Class) actualType:Object.class;
                } 
            }
        }

        parser.parseArray(componentType, array, fieldName);
        
        return (T) toObjectArray(parser, componentClass, array);
    }

