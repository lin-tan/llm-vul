    static public File allocateFile(File dir, String name) {
        int q = name.indexOf('?');
        name = q > 0? name.substring(0, q):name;
    
        int dot = name.indexOf('.');
        String prefix;
        String suffix;

        if(dot >= 0){
            prefix  =  name.substring(0, dot);
            suffix = name.substring(dot);
        }else{
            prefix = name;
            suffix = "";
        }

        
        File file = new File(dir, name);
        for(int index = 2;file.exists();index++){
            file = new File(dir, prefix + "-" + index + suffix);
        }
   
        File parentFile = file.getParentFile();
        parentFile.mkdirs();
        
        return file;
    }
