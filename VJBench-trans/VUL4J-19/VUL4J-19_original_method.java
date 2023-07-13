@Override
public void prepareForDecryption(PDEncryption encryption, COSArray documentIDArray,
                                 DecryptionMaterial decryptionMaterial)
                                 throws IOException
{
    if(!(decryptionMaterial instanceof StandardDecryptionMaterial))
    {
        throw new IOException("Decryption material is not compatible with the document");
    }
    setDecryptMetadata(encryption.isEncryptMetaData());
    StandardDecryptionMaterial material = (StandardDecryptionMaterial)decryptionMaterial;
    String password = material.getPassword();
    if(password == null)
    {
        password = "";
    }
    int dicPermissions = encryption.getPermissions();
    int dicRevision = encryption.getRevision();
    int dicLength = encryption.getVersion() == 1 ? 5 : encryption.getLength() / 8;
    byte[] documentIDBytes = getDocumentIDBytes(documentIDArray);
    boolean encryptMetadata = encryption.isEncryptMetaData();
    byte[] userKey = encryption.getUserKey();
    byte[] ownerKey = encryption.getOwnerKey();
    byte[] ue = null, oe = null;
    Charset passwordCharset = Charsets.ISO_8859_1;
    if (dicRevision == 6 || dicRevision == 5)
    {
        passwordCharset = Charsets.UTF_8;
        ue = encryption.getUserEncryptionKey();
        oe = encryption.getOwnerEncryptionKey();
    }
    AccessPermission currentAccessPermission;
    if( isOwnerPassword(password.getBytes(passwordCharset), userKey, ownerKey,
                             dicPermissions, documentIDBytes, dicRevision,
                             dicLength, encryptMetadata) )
    {
        currentAccessPermission = AccessPermission.getOwnerAccessPermission();
        setCurrentAccessPermission(currentAccessPermission);
        byte[] computedPassword;
        if (dicRevision == 6 || dicRevision == 5)
        {
            computedPassword = password.getBytes(passwordCharset);
        }
        else
        {
            computedPassword = getUserPassword(password.getBytes(passwordCharset),
                    ownerKey, dicRevision, dicLength );
        }
        encryptionKey =
            computeEncryptedKey(
                computedPassword,
                ownerKey, userKey, oe, ue,
                dicPermissions,
                documentIDBytes,
                dicRevision,
                dicLength,
                encryptMetadata, true );
    }
    else if( isUserPassword(password.getBytes(passwordCharset), userKey, ownerKey,
                       dicPermissions, documentIDBytes, dicRevision,
                       dicLength, encryptMetadata) )
    {
        currentAccessPermission = new AccessPermission(dicPermissions);
        setCurrentAccessPermission(currentAccessPermission);
        
        encryptionKey =
            computeEncryptedKey(
                password.getBytes(passwordCharset),
                ownerKey, userKey, oe, ue,
                dicPermissions,
                documentIDBytes,
                dicRevision,
                dicLength,
                encryptMetadata, false );
    }
    else
    {
        throw new InvalidPasswordException("Cannot decrypt PDF, the password is incorrect");
    }
    if (dicRevision == 6 || dicRevision == 5)
    {
        validatePerms(encryption, dicPermissions, encryptMetadata);
    }
    if (encryption.getVersion() == 4 || encryption.getVersion() == 5)
    {
        PDCryptFilterDictionary stdCryptFilterDictionary = encryption.getStdCryptFilterDictionary();
        if (stdCryptFilterDictionary != null)
        {
            COSName cryptFilterMethod = stdCryptFilterDictionary.getCryptFilterMethod();
            setAES(COSName.AESV2.equals(cryptFilterMethod) || 
                   COSName.AESV3.equals(cryptFilterMethod));
        }
    }
}
