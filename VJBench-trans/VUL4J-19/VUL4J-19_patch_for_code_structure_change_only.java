public void prepareForDecryption(PDEncryption encryption, COSArray documentIDArray,
                                    DecryptionMaterial decryptionMaterial)
                                    throws IOException
{
    if((decryptionMaterial instanceof StandardDecryptionMaterial))
    {
        
        byte[] userKey = encryption.getUserKey();
        byte[] ownerKey = encryption.getOwnerKey();

        StandardDecryptionMaterial material = (StandardDecryptionMaterial)decryptionMaterial;
        String password = material.getPassword();
    
        boolean encryptMetadata = encryption.isEncryptMetaData();
        setDecryptMetadata(encryptMetadata);
        
        if(password == null){
            password = "";
        }

        int dicLength = 0;
        if(encryption.getVersion() != 1 ){
            dicLength = encryption.getLength() / 8;
        }else{
            dicLength = 5;
        } 


        byte[] documentIDBytes = getDocumentIDBytes(documentIDArray);    
        int dicPermissions = encryption.getPermissions();
        int dicRevision = encryption.getRevision();
        byte[] ue = null;
        byte[] oe = null;
        Charset passwordCharset = Charsets.ISO_8859_1;

        if (dicRevision == 6 || dicRevision == 5)
        {
            passwordCharset = Charsets.UTF_8;
            ue = encryption.getUserEncryptionKey();
            oe = encryption.getOwnerEncryptionKey();
        }
        
        byte[] passwordByteArray = password.getBytes(passwordCharset);           
        if( !isOwnerPassword(passwordByteArray, userKey, ownerKey,
            dicPermissions, documentIDBytes, dicRevision,
            dicLength, encryptMetadata) )
            {
                if( !isUserPassword(passwordByteArray, userKey, ownerKey,
                    dicPermissions, documentIDBytes, dicRevision,
                    dicLength, encryptMetadata) )
                {
                    throw new InvalidPasswordException("Cannot decrypt PDF, the password is incorrect");

                } else 
                {
                    AccessPermission currentAccessPermission = new AccessPermission(dicPermissions);
                    currentAccessPermission.setReadOnly();
                    setCurrentAccessPermission(currentAccessPermission);
                    encryptionKey = computeEncryptedKey( passwordByteArray, ownerKey, userKey, oe, ue, dicPermissions,documentIDBytes,
                            dicRevision,dicLength, encryptMetadata, false );
                }

            }else{
                setCurrentAccessPermission(AccessPermission.getOwnerAccessPermission());
                byte[] computedPassword;

                if ( dicRevision != 6 && dicRevision != 5)
                {
                    computedPassword = getUserPassword(passwordByteArray,
                    ownerKey, dicRevision, dicLength );
                }
                else
                {
                    computedPassword = passwordByteArray;
            
                }

                encryptionKey = computeEncryptedKey(computedPassword, 
                ownerKey, userKey, oe, ue, dicPermissions,
                documentIDBytes, dicRevision,dicLength, encryptMetadata, true );
            }
        
    

        if (dicRevision == 6 || dicRevision == 5)
        {
            validatePerms(encryption, dicPermissions, encryptMetadata);
        }            

        if (encryption.getVersion() != 4 && encryption.getVersion() != 5)
        {
            return;

        }else{

            PDCryptFilterDictionary stdCryptFilterDictionary = encryption.getStdCryptFilterDictionary();

            if (stdCryptFilterDictionary == null)
            {
                return;
            }

            COSName cryptFilterMethod = stdCryptFilterDictionary.getCryptFilterMethod();
            boolean aesv2 = COSName.AESV2.equals(cryptFilterMethod);
            boolean aesv3 = COSName.AESV3.equals(cryptFilterMethod);
            setAES( aesv2 || aesv3 );

        }
    }else{
        throw new IOException("Decryption material is not compatible with the document");
    }
}