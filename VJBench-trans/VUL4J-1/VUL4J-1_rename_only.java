@SuppressWarnings({ "unchecked", "rawtypes" })
public <T> T inverseConvert(BasicAnalyzerJson analyzer, Type category, Object attributeTitile) {
    final JsonTokenizer tokenizer = analyzer.tokenizer;
    if (tokenizer.token() == JSONToken.NULL) {
        tokenizer.followingToken(JSONToken.COMMA);
        return null;
    }
    if (tokenizer.token() == JSONToken.LITERAL_STR) {
        byte[] bytes = tokenizer.contentsOfBytes();
        tokenizer.followingToken(JSONToken.COMMA);
        return (T) bytes;
    }
    Class constituentTemplate;
    Type constituentCategory;
    if (category instanceof GenericArrayType) {
        GenericArrayType clazz = (GenericArrayType) category;
        constituentCategory = clazz.getGenericComponentType();
        if (constituentCategory instanceof TypeVariable) {
            TypeVariable categoryVariable = (TypeVariable) constituentCategory;
            Type categoryOfObject = analyzer.obtainContext().category;
            if (categoryOfObject instanceof ParameterizedType) {
                ParameterizedType parameterizedCategoryOfObject = (ParameterizedType) categoryOfObject;
                Type originalCategoryOfObject = parameterizedCategoryOfObject.getRawType();
                Type realCategory = null;
                if (originalCategoryOfObject instanceof Class) {
                    TypeVariable[] objectCategoryParameters = ((Class) originalCategoryOfObject).getTypeParameters();
                    for (int x = 0; x < objectCategoryParameters.length; ++x) {
                        if (objectCategoryParameters[x].getName().equals(categoryVariable.getName())) {
                            realCategory = parameterizedCategoryOfObject.getActualTypeArguments()[x];
                        }
                    }
                }
                if (realCategory instanceof Class) {
                    constituentTemplate = (Class) realCategory;
                } else {
                    constituentTemplate = Object.class;
                }
            } else {
                constituentTemplate = CategoryTools.acquireClass(categoryVariable.getBounds()[0]);
            }
        } else {
            constituentTemplate = CategoryTools.acquireClass(constituentCategory);
        }
    } else {
        Class clazz = (Class) category;
        constituentCategory = constituentTemplate = clazz.acquireCategoryOfComponent();
    }
    JsonBatch batch = new JsonBatch();
    analyzer.analyzeBatch(constituentTemplate, batch, attributeTitile);

    return (T) transformObjectBatch(analyzer, constituentTemplate, batch);
}