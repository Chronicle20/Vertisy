package tools;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Map;

import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class AutoJCE{

	/**
	 * Credits: ntoskrnl of StackOverflow
	 * http://stackoverflow.com/questions/1179672/
	 */
	public static void removeCryptographyRestrictions(){
		if(!isRestrictedCryptography()){
			System.out.println("Cryptography restrictions removal not needed");
			return;
		}
		try{
			/*
			 * Do the following, but with reflection to bypass access checks:
			 *
			 * JceSecurity.isRestricted = false;
			 * JceSecurity.defaultPolicy.perms.clear();
			 * JceSecurity.defaultPolicy.add(CryptoAllPermission.INSTANCE);
			 */
			final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
			final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
			final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");
			final Field isRestrictedField = jceSecurity.getDeclaredField("isRestricted");// was set to final in Java 8 Update 112. Requires you to remove the final modifier.
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(isRestrictedField, isRestrictedField.getModifiers() & ~Modifier.FINAL);
			isRestrictedField.setAccessible(true);
			isRestrictedField.set(null, false);
			final Field defaultPolicyField = jceSecurity.getDeclaredField("defaultPolicy");
			defaultPolicyField.setAccessible(true);
			final PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);
			final Field perms = cryptoPermissions.getDeclaredField("perms");
			perms.setAccessible(true);
			((Map<?, ?>) perms.get(defaultPolicy)).clear();
			final Field instance = cryptoAllPermission.getDeclaredField("INSTANCE");
			instance.setAccessible(true);
			defaultPolicy.add((Permission) instance.get(null));
			System.out.println("Successfully removed cryptography restrictions");
		}catch(final Exception e){
			System.err.println("Failed to remove cryptography restrictions");
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	private static boolean isRestrictedCryptography(){
		// This simply matches the Oracle JRE, but not OpenJDK.
		return "Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"));
	}
}
