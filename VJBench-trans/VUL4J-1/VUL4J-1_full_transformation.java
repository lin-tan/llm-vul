    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T inverseConvert(BasicAnalyzerJson analyzer, Type category, Object attributeTitile) {
        final JsonTokenizer tokenizer = analyzer.tokenizer;
        switch (tokenizer.token()) {
            case JSONToken.LITERAL_STR:
                byte[] bytes = tokenizer.contentsOfBytes();
                tokenizer.followingToken(JSONToken.COMMA);
                return (T) bytes;
            case JSONToken.NULL:
                tokenizer.followingToken(JSONToken.COMMA);
                return null;
            default:
                break;
        }

        JsonBatch batch = new JsonBatch();
        Class constituentTemplate;
        Type constituentCategory;
        if (!(category instanceof GenericArrayType)) {
            constituentCategory =  ((Class) category).acquireCategoryOfComponent();
            constituentTemplate = ((Class) category).acquireCategoryOfComponent();
        } else {
            constituentCategory = ((GenericArrayType) category).getGenericComponentType();
            if (! (constituentCategory instanceof TypeVariable)) {
                constituentTemplate = CategoryTools.acquireClass(constituentCategory);
            } else {
                   
                TypeVariable categoryVariable = (TypeVariable) constituentCategory;
                Type categoryOfObject = analyzer.obtainContext().category;
                if (!(categoryOfObject instanceof ParameterizedType)) {
                    Type type_ = categoryVariable.getBounds()[0];
                    constituentTemplate = CategoryTools.acquireClass(type_);
              
                } else {
          
                    ParameterizedType parameterizedCategoryOfObject = (ParameterizedType) categoryOfObject;
                    Type originalCategoryOfObject = parameterizedCategoryOfObject.getRawType();
                    Type realCategory = null;
                    if (originalCategoryOfObject instanceof Class) {
                        TypeVariable[] objectCategoryParameters = ((Class) originalCategoryOfObject).getTypeParameters();
                        int x = 0;
                        while( x < objectCategoryParameters.length) {
                            String i_name = objectCategoryParameters[x].getName();
                            String typeVar_name = categoryVariable.getName();
                            if (!i_name.equals(typeVar_name)) {
                                x++;
                               continue;
                            }
                            realCategory = parameterizedCategoryOfObject.getActualTypeArguments()[x];
                            x++;
                        }
                    }
                    
                    constituentTemplate = (realCategory instanceof Class)? (Class) realCategory:Object.class;
                } 
            }
        }

        analyzer.analyzeBatch(constituentTemplate, batch, attributeTitile);
        
        return (T) transformObjectBatch(analyzer, constituentTemplate, batch);
    }