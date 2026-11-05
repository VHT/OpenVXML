package org.eclipse.vtp.framework.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Describes the jar or project directory that contributed extensions to an
 * {@link ExtensionRegistry}. Replaces the OSGi bundle for the purposes of
 * loading classes and entries.
 */
public final class Contributor {
	/** The symbolic name of the contributor. */
	private final String name;
	/** The <code>Bundle-Name</code> of the contributor, or <code>null</code>. */
	private final String bundleName;
	/** The class loader that loads the contributor's classes. */
	private final ClassLoader classLoader;
	/** The root of the jar or project directory, always ending in "/". */
	private final URL root;

	/**
	 * Creates a new Contributor.
	 *
	 * @param name
	 *            The symbolic name of the contributor.
	 * @param bundleName
	 *            The <code>Bundle-Name</code> of the contributor, or
	 *            <code>null</code>.
	 * @param classLoader
	 *            The (webapp) class loader that loads the contributor's
	 *            classes.
	 * @param root
	 *            The root of the jar or project directory.
	 */
	Contributor(String name, String bundleName, ClassLoader classLoader,
			URL root) {
		this.name = name;
		this.bundleName = bundleName;
		this.classLoader = classLoader;
		this.root = root;
	}

	/**
	 * Returns the symbolic name of this contributor.
	 *
	 * @return The symbolic name of this contributor.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the symbolic name of this contributor.
	 *
	 * @return The symbolic name of this contributor.
	 */
	public String getSymbolicName() {
		return name;
	}

	/**
	 * Returns the <code>Bundle-Name</code> of this contributor, or
	 * <code>null</code> if it has none.
	 *
	 * @return The <code>Bundle-Name</code> of this contributor.
	 */
	public String getBundleName() {
		return bundleName;
	}

	/**
	 * Loads the named class through the webapp class loader.
	 *
	 * @param className
	 *            The name of the class to load.
	 * @return The loaded class.
	 * @throws ClassNotFoundException
	 *             If the class cannot be found.
	 */
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return classLoader.loadClass(className);
	}

	/**
	 * Returns the URL of the entry at the given path, relative to the jar root
	 * or project directory.
	 *
	 * @param path
	 *            The path of the entry.
	 * @return The URL of the entry, or <code>null</code> if it does not exist.
	 */
	public URL getEntry(String path) {
		try {
			URL url = new URL(root, normalize(path));
			InputStream input = null;
			try {
				input = url.openStream();
			} finally {
				if (input != null) {
					input.close();
				}
			}
			return url;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the paths of the entries directly under the given directory
	 * path, relative to the jar root or project directory. Directory paths
	 * end with "/".
	 *
	 * @param path
	 *            The directory path.
	 * @return The paths of the entries, or <code>null</code> if there are none
	 *         or the directory does not exist.
	 */
	public Enumeration<String> getEntryPaths(String path) {
		String prefix = normalize(path);
		if (prefix.length() > 0 && !prefix.endsWith("/")) {
			prefix = prefix + "/";
		}
		Set<String> paths = new LinkedHashSet<String>();
		try {
			if ("file".equals(root.getProtocol())) {
				java.io.File directory = new java.io.File(
						new java.io.File(root.toURI()), prefix);
				java.io.File[] children = directory.listFiles();
				if (children == null) {
					return null;
				}
				java.util.Arrays.sort(children);
				for (java.io.File child : children) {
					paths.add(prefix + child.getName()
							+ (child.isDirectory() ? "/" : ""));
				}
			} else if ("jar".equals(root.getProtocol())) {
				JarURLConnection connection = (JarURLConnection) root
						.openConnection();
				connection.setUseCaches(false);
				JarFile jar = connection.getJarFile();
				try {
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						String entry = entries.nextElement().getName();
						if (entry.length() <= prefix.length()
								|| !entry.startsWith(prefix)) {
							continue;
						}
						int end = entry.indexOf('/', prefix.length());
						paths.add(end < 0 ? entry : entry.substring(0,
								end + 1));
					}
				} finally {
					jar.close();
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
		if (paths.isEmpty()) {
			return null;
		}
		List<String> list = new ArrayList<String>(paths);
		return Collections.enumeration(list);
	}

	/**
	 * Returns the URL of the resource at the given path, relative to the jar
	 * root or project directory.
	 *
	 * @param path
	 *            The path of the resource.
	 * @return The URL of the resource, or <code>null</code> if it does not
	 *         exist.
	 */
	public URL getResource(String path) {
		return getEntry(path);
	}

	private static String normalize(String path) {
		if (path == null) {
			return "";
		}
		int start = 0;
		while (start < path.length() && path.charAt(start) == '/') {
			++start;
		}
		return path.substring(start);
	}

	@Override
	public String toString() {
		return name + " [" + root + "]";
	}
}
