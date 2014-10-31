package org.echovantage.gild.stage;

import java.nio.file.Path;

import org.echovantage.gild.proxy.ServiceProxy;

/**
 * Manages the paths used by a stage during a Glid test execution. It's
 * important that none of these methods return
 * null. They may throw an exception if they truly have no analog in the file
 * system. They should be returned whether
 * they exist, it's simply where in the file system they would occur, regardless
 * of whether they actually do.
 *
 * @author fuwjax
 */
public interface Stage {
	/**
	 * The input path for the service.
	 *
	 * @param service
	 *           the service name of one of the registered {@link ServiceProxy}
	 *           instances
	 * @return the input path, never null
	 */
	Path inputPath(String service);

	/**
	 * The gold copy path for the service.
	 *
	 * @param service
	 *           the service name of one of the registered {@link ServiceProxy}
	 *           instances
	 * @return the gold copy path, never null
	 */
	Path goldPath(String service);

	/**
	 * The compare copy (transformed output) path for the service.
	 *
	 * @param service
	 *           the service name of one of the registered {@link ServiceProxy}
	 *           instances
	 * @return the compare copy path, never null
	 */
	Path comparePath(String service);

	/**
	 * The pre-transform (raw output) path for the service.
	 *
	 * @param service
	 *           the service name of one of the registered {@link ServiceProxy}
	 *           instances
	 * @return the pre-transform path, never null
	 */
	Path transformPath(String service);

	/**
	 * The next stage for this test, named {@code stage}.
	 *
	 * @param stage
	 *           the next stage name
	 * @return the next stage.
	 */
	Stage nextStage(String stage);
}
