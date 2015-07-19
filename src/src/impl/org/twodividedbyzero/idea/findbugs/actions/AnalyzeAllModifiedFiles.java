/*
 * Copyright 2008-2015 Andre Pfeiler
 *
 * This file is part of FindBugs-IDEA.
 *
 * FindBugs-IDEA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FindBugs-IDEA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FindBugs-IDEA.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.twodividedbyzero.idea.findbugs.actions;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.util.IdeaUtilImpl;
import org.twodividedbyzero.idea.findbugs.core.FindBugsPlugin;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;
import org.twodividedbyzero.idea.findbugs.core.FindBugsStarter;
import org.twodividedbyzero.idea.findbugs.core.FindBugsState;
import org.twodividedbyzero.idea.findbugs.preferences.FindBugsPreferences;

import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public final class AnalyzeAllModifiedFiles extends AbstractAnalyzeAction {

	@Override
	void updateImpl(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@Nullable final Module module,
			@NotNull final FindBugsPlugin plugin,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state,
			@NotNull final FindBugsPreferences preferences
	) {

		boolean enable = false;
		final List<VirtualFile> modifiedFiles = IdeaUtilImpl.getAllModifiedFiles(e.getDataContext());
		if (state.isIdle() && !modifiedFiles.isEmpty()) {
			for (final VirtualFile virtualFile : modifiedFiles) {
				if (IdeaUtilImpl.isValidFileType(virtualFile.getFileType())) {
					enable = true;
					break;
				}
			}
		}

		e.getPresentation().setEnabled(enable);
		e.getPresentation().setVisible(true);
	}


	@SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
	@Override
	void analyze(
			@NotNull final AnActionEvent e,
			@NotNull final Project project,
			@Nullable final Module module,
			@NotNull final FindBugsPlugin plugin,
			@NotNull final ToolWindow toolWindow,
			@NotNull final FindBugsState state,
			@NotNull final FindBugsPreferences preferences
	) {

		final VirtualFile[] files = IdeaUtilImpl.getProjectClasspath(e.getDataContext());
		final List<VirtualFile> modifiedFiles = IdeaUtilImpl.getAllModifiedFiles(e.getDataContext());

		new FindBugsStarter(project, "Running FindBugs analysis for modified files...", preferences) {
			@Override
			protected void configure(@NotNull ProgressIndicator indicator, @NotNull FindBugsProject findBugsProject) {
				findBugsProject.configureAuxClasspathEntries(indicator, files);
				findBugsProject.configureSourceDirectories(indicator, modifiedFiles);
				findBugsProject.configureOutputFiles(project, modifiedFiles);
			}
		}.start();
	}
}
