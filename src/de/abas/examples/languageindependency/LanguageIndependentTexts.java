package de.abas.examples.languageindependency;

import java.util.Locale;
import java.util.ResourceBundle;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.EKS;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.api.gui.TextBox;

/**
 * This class shows how to use language independent texts. The translation of each text is stored in a Property file with the locale as extension in the file.
 *
 * Property files can be translated in abas ERP using the infosystem TRANS.
 *
 * @author abas Software AG
 *
 */
public class LanguageIndependentTexts implements ContextRunnable {

	@Override
	public int runFop(FOPSessionContext ctx, String[] args) throws FOPException {
		new TextBox(ctx.getDbContext(), getTextInOperatingLanguage("LanguageIndependentTexts.1", null), getTextInOperatingLanguage("LanguageIndependentTexts.2", null)).show();
		new TextBox(ctx.getDbContext(), getTextInOperatingLanguage("LanguageIndependentTexts.1", null), getTextInOperatingLanguage("LanguageIndependentTexts.3",
				new String[] { "parameter" })).show();
		return 0;
	}

	/**
	 * Gets text specified by key from LanguageIndependentTexts_lang.properties in current operating language
	 *
	 * @param key The key of the text in ControlFOPCopySystem.properties.
	 * @param params The array containing the Strings to change for the placeholder within the bundle.
	 * @return Returns text in current operating language.
	 */
	protected String getTextInOperatingLanguage(String key, String[] params) {
		// gets current operating language
		Locale operatingLangLocale = EKS.getFOPSessionContext().getOperatingLangLocale();
		// gets text specified by key from ControlFOPCopySystem.properties in
		// previously specified operating language
		String bundle = ResourceBundle.getBundle(LanguageIndependentTexts.class.getName(), operatingLangLocale, LanguageIndependentTexts.class.getClassLoader()).getString(key);
		// fills replacement characters with the according parameters from
		// params
		if (!(params == null)) {
			if (!(params.length == 0)) {
				for (String param : params) {
					bundle = bundle.replaceFirst("\\{[0-9]+\\}", param);
				}
			}
		}
		return bundle;
	}

}
