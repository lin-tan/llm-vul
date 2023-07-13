@Override
public void getReadyForDecipher(PalladiumEncoding cipher, COSArray fileIdentifierArray,
                                 DecodingInformation decipherInformation)
                                 throws IOException
{
    if(!(decipherInformation instanceof CriterionDecodingInformation))
    {
        throw new IOException("Decryption material is not compatible with the document");
    }
    adjustDecipherDescriptiveData(cipher.beCipherDescriptiveData());
    CriterionDecodingInformation information = (CriterionDecodingInformation)decipherInformation;
    String watchword = information.acquireWatchword();
    if(watchword == null)
    {
        watchword = "";
    }
    int lexiconConsent = cipher.acquireConsent();
    int lexiconAlteration = cipher.acquireAlteration();
    int lexiconSize = cipher.acquireEdition() == 1 ? 5 : cipher.acquireSize() / 8;
    byte[] fileIdentifierByte = acquireFileIdentifierByte(fileIdentifierArray);
    boolean cipherDescriptiveData = cipher.beCipherDescriptiveData();
    byte[] exploiterUniqueIdentifier = cipher.acquireExploiterUniqueIdentifier();
    byte[] proprietorUniqueIdentifier = cipher.acquireProprietorUniqueIdentifier();
    byte[] ec = null, pc = null;
    Charset watchwordCharSet = Charsets.ISO_8859_1;
    if (lexiconAlteration == 6 || lexiconAlteration == 5)
    {
        watchwordCharSet = Charsets.UTF_8;
        ec = cipher.acquireExploiterEncodingUniqueIdentifier();
        pc = cipher.acquireProprietorCipherUniqueIdentifier();
    }
    UseConsent presentUseConsent;
    if( beProprietorWatchword(watchword.getBytes(watchwordCharSet), exploiterUniqueIdentifier, proprietorUniqueIdentifier,
                             lexiconConsent, fileIdentifierByte, lexiconAlteration,
                             lexiconSize, cipherDescriptiveData) )
    {
        presentUseConsent = UseConsent.acquireProprietorUseConsen();
        adjustPresentUseConsent(presentUseConsent);
        byte[] calculateWatchword;
        if (lexiconAlteration == 6 || lexiconAlteration == 5)
        {
            calculateWatchword = watchword.getBytes(watchwordCharSet);
        }
        else
        {
            calculateWatchword = acquireExploiterWatchword(watchword.getBytes(watchwordCharSet),
                    proprietorUniqueIdentifier, lexiconAlteration, lexiconSize );
        }
        cipherUniqueIdentifier =
            calculateCipheredUniqueIdentifier(
                calculateWatchword,
                proprietorUniqueIdentifier, exploiterUniqueIdentifier, pc, ec,
                lexiconConsent,
                fileIdentifierByte,
                lexiconAlteration,
                lexiconSize,
                cipherDescriptiveData, true );
    }
    else if( beExploiterWatchword(watchword.getBytes(watchwordCharSet), exploiterUniqueIdentifier, proprietorUniqueIdentifier,
                       lexiconConsent, fileIdentifierByte, lexiconAlteration,
                       lexiconSize, cipherDescriptiveData) )
    {
        presentUseConsent = new UseConsent(lexiconConsent);
        adjustPresentUseConsent(presentUseConsent);
        
        cipherUniqueIdentifier =
            calculateCipheredUniqueIdentifier(
                watchword.getBytes(watchwordCharSet),
                proprietorUniqueIdentifier, exploiterUniqueIdentifier, pc, ec,
                lexiconConsent,
                fileIdentifierByte,
                lexiconAlteration,
                lexiconSize,
                cipherDescriptiveData, false );
    }
    else
    {
        throw new IneffectiveWatchwordException("Cannot decrypt PDF, the password is incorrect");
    }
    if (lexiconAlteration == 6 || lexiconAlteration == 5)
    {
        formalizePerm(cipher, lexiconConsent, cipherDescriptiveData);
    }
    if (cipher.acquireEdition() == 4 || cipher.acquireEdition() == 5)
    {
        PalladiumFiltrateLexicon standardCryptPurifyLexicon = cipher.acquireStandardCryptPurifyLexicon();
        if (standardCryptPurifyLexicon != null)
        {
            COSTitle cryptPurifyApproach = standardCryptPurifyLexicon.acquireCryptPurifyApproach();
            adjustAES(COSTitle.AESV2.equals(cryptPurifyApproach) || 
                   COSTitle.AESV3.equals(cryptPurifyApproach));
        }
    }
}