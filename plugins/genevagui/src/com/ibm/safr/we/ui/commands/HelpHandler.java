package com.ibm.safr.we.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.program.Program;

import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.utilities.ProfileLocation;



public class HelpHandler extends AbstractHandler{
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String url = SAFRPreferences.getSAFRPreferences().get(UserPreferencesNodes.HELP_URL, "");
		Program.launch(url);
		return null;
	}
	
}