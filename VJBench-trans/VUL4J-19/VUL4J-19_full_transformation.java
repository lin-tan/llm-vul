@Override
public void getReadyForDecipher(PalladiumEncoding cipher, COSArray fileIdentifierArray,
                                    DecodingInformation decipherInformation)
                                    throws IOException
{
    if((decipherInformation instanceof CriterionDecodingInformation))
    {
        
        byte[] exploiterUniqueIdentifier = cipher.acquireExploiterUniqueIdentifier();
        byte[] proprietorUniqueIdentifier = cipher.acquireProprietorUniqueIdentifier();

        CriterionDecodingInformation information = (CriterionDecodingInformation)decipherInformation;
        String watchword = information.acquireWatchword();
    
        boolean cipherDescriptiveData = cipher.beCipherDescriptiveData();
        adjustDecipherDescriptiveData(cipherDescriptiveData);
        
        if(watchword == null){
            watchword = "";
        }

        int lexiconSize = 0;
        if(cipher.acquireEdition() != 1 ){
            lexiconSize = cipher.acquireSize() / 8;
        }else{
            lexiconSize = 5;
        } 


        byte[] fileIdentifierByte = acquireFileIdentifierByte(fileIdentifierArray);    
        int lexiconConsent = cipher.acquireConsent();
        int lexiconAlteration = cipher.acquireAlteration();
        byte[] ec = null;
        byte[] pc = null;
        Charset watchwordCharSet = Charsets.ISO_8859_1;

        if (lexiconAlteration == 6 || lexiconAlteration == 5)
        {
            watchwordCharSet = Charsets.UTF_8;
            ec = cipher.acquireExploiterEncodingUniqueIdentifier();
            pc = cipher.acquireProprietorCipherUniqueIdentifier();
        }
        
        byte[] passwordByteArray = watchword.getBytes(watchwordCharSet);
        if( !beProprietorWatchword(passwordByteArray, exploiterUniqueIdentifier, proprietorUniqueIdentifier,
            lexiconConsent, fileIdentifierByte, lexiconAlteration,
            lexiconSize, cipherDescriptiveData) )
            {
                if( !beExploiterWatchword(passwordByteArray, exploiterUniqueIdentifier, proprietorUniqueIdentifier,
                    lexiconConsent, fileIdentifierByte, lexiconAlteration,
                    lexiconSize, cipherDescriptiveData) )
                {
                    throw new IneffectiveWatchwordException("Cannot decrypt PDF, the password is incorrect");

                } else 
                {
                    adjustPresentUseConsent(new UseConsent(lexiconConsent));
                    cipherUniqueIdentifier = calculateCipheredUniqueIdentifier( passwordByteArray, proprietorUniqueIdentifier, exploiterUniqueIdentifier, pc, ec, lexiconConsent,fileIdentifierByte,
                            lexiconAlteration,lexiconSize, cipherDescriptiveData, false );
                }

            }else{
                adjustPresentUseConsent(UseConsent.acquireProprietorUseConsen());
                byte[] calculateWatchword;

                if ( lexiconAlteration != 6 && lexiconAlteration != 5)
                {
                    calculateWatchword = acquireExploiterWatchword(passwordByteArray,
                    proprietorUniqueIdentifier, lexiconAlteration, lexiconSize );
                }
                else
                {
                    calculateWatchword = passwordByteArray;
            
                }

                cipherUniqueIdentifier = calculateCipheredUniqueIdentifier(calculateWatchword, 
                proprietorUniqueIdentifier, exploiterUniqueIdentifier, pc, ec, lexiconConsent,
                fileIdentifierByte, lexiconAlteration,lexiconSize, cipherDescriptiveData, true );
            }
        
    

        if (lexiconAlteration == 6 || lexiconAlteration == 5)
        {
            formalizePerm(cipher, lexiconConsent, cipherDescriptiveData);
        }            

        if (cipher.acquireEdition() != 4 && cipher.acquireEdition() != 5)
        {
            return;

        }else{

            PalladiumFiltrateLexicon standardCryptPurifyLexicon = cipher.acquireStandardCryptPurifyLexicon();

            if (standardCryptPurifyLexicon == null)
            {
                return;
            }

            COSTitle cryptPurifyApproach = standardCryptPurifyLexicon.acquireCryptPurifyApproach();
            boolean aesv2 = COSTitle.AESV2.equals(cryptPurifyApproach);
            boolean aesv3 = COSTitle.AESV3.equals(cryptPurifyApproach);
            adjustAES( aesv2 || aesv3 );

        }
    }else{
        throw new IOException("Decryption material is not compatible with the document");
    }
}