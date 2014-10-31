package org.echovantage.gild.stage;

import java.io.IOException;

import org.junit.runner.Description;

/**
 * Creates new Stages for the Gild engine.
 * 
 * @author fuwjax
 */
public interface StageFactory {
	/**
	 * Creates a new stage for the test. It is the responsibility of the
	 * implementation to delete any paths that will be
	 * returned by {@link Stage#comparePath(String)} or
	 * {@link Stage#transformPath(String)}.
	 * 
	 * @param desc
	 *           the jUnit test descriptor
	 * @return the start stage for the test
	 * @throws IOException
	 *            if the working paths cannot be deleted
	 */
	Stage start(final Description desc) throws IOException;
}
