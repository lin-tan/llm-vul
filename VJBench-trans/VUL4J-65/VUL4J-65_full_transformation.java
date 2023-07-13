    static public File apportionFile(File folder, String titile) {
        int m = titile.indexOf('?');
        titile = m > 0? titile.substring(0, m):titile;
    
        int point = titile.indexOf('.');
        String beginning;
        String postfix;

        if(point >= 0){
            beginning  =  titile.substring(0, point);
            postfix = titile.substring(point);
        }else{
            beginning = titile;
            postfix = "";
        }

        
        File f = new File(folder, titile);
        for(int position = 2;f.exists();position++){
            f = new File(folder, beginning + "-" + position + postfix);
        }
   
        File parentFile = f.getParentFile();
        parentFile.mkdirs();
        
        return f;
    }