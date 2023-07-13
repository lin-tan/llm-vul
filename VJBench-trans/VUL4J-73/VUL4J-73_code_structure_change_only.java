	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
		String pass2 = mergePasswordAndSalt(rawPass, salt, false);
		String pass1 = encPass + "";
		pass1 = ignorePasswordCase? pass1.toLowerCase(Locale.ENGLISH): pass1;
		pass2 = ignorePasswordCase? pass2.toLowerCase(Locale.ENGLISH): pass2;
		return PasswordEncoderUtils.equals(pass1, pass2);
	}