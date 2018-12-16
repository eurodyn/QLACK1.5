package com.eurodyn.qlack2.webdesktop.api.dto;

import java.io.Serializable;

/**
 * Provides information regarding how an application should be treated by the
 * Web Desktop. This class is created by the webdesktop-impl when discovering
 * a newly deployed application by reading the respective configuration
 * properties of the deployed application from OSGI-INF/wd-app.yaml.
 * @author European Dynamics SA
 *
 */
public class ApplicationInfo implements Serializable {
	private static final long serialVersionUID = -3853756343460422928L;
	public enum DISPLAY_ON {group, item}

	private transient Identification identification;
	private transient Instantiation instantiation;
	private transient Menu menu;
	private transient Window window;
	private boolean active = true;

	public static class Identification {
		private String uniqueId;
		private String titleKey;
		private String descriptionKey;
		private String version;
		public String getUniqueId() {
			return uniqueId;
		}
		public void setUniqueId(String uniqueId) {
			this.uniqueId = uniqueId;
		}
		public String getTitleKey() {
			return titleKey;
		}
		public void setTitleKey(String titleKey) {
			this.titleKey = titleKey;
		}
		public String getDescriptionKey() {
			return descriptionKey;
		}
		public void setDescriptionKey(String descriptionKey) {
			this.descriptionKey = descriptionKey;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
	}

	public static class Instantiation {
		private String path;
		private String index;
		private boolean multipleInstances;
		private String configPath;
		private boolean restrictAccess;
		private String translationsGroup;

		public boolean getRestrictAccess() {
			return restrictAccess;
		}
		public void setRestrictAccess(boolean restrictAccess) {
			this.restrictAccess = restrictAccess;
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public String getIndex() {
			return index;
		}
		public void setIndex(String index) {
			this.index = index;
		}
		public boolean getMultipleInstances() {
			return multipleInstances;
		}
		public void setMultipleInstances(boolean multipleInstances) {
			this.multipleInstances = multipleInstances;
		}
		public String getConfigPath() {
			return configPath;
		}
		public void setConfigPath(String configPath) {
			this.configPath = configPath;
		}
		public String getTranslationsGroup() {
			return translationsGroup;
		}
		public void setTranslationsGroup(String translationsGroup) {
			this.translationsGroup = translationsGroup;
		}
	}

	public static class Menu {
		private String icon;
		private String iconSmall;
		private Type type;
		public String getIcon() {
			return icon;
		}
		public void setIcon(String icon) {
			this.icon = icon;
		}
		public String getIconSmall() {
			return iconSmall;
		}
		public void setIconSmall(String iconSmall) {
			this.iconSmall = iconSmall;
		}
		public Type getType() {
			return type;
		}
		public void setType(Type type) {
			this.type = type;
		}

		public static class Type {
			private DISPLAY_ON displayOn;
			private String groupKey;
			public DISPLAY_ON getDisplayOn() {
				return displayOn;
			}
			public void setDisplayOn(DISPLAY_ON displayOn) {
				this.displayOn = displayOn;
			}
			public String getGroupKey() {
				return groupKey;
			}
			public void setGroupKey(String groupKey) {
				this.groupKey = groupKey;
			}
		}
	}

	public static class Window {
		private int width;
		private int minWidth;
		private int height;
		private int minHeight;
		private boolean resizable;
		private boolean maximizable;
		private boolean minimizable;
		private boolean closable;
		private boolean draggable;
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public int getMinWidth() {
			return minWidth;
		}
		public void setMinWidth(int minWidth) {
			this.minWidth = minWidth;
		}
		public int getHeight() {
			return height;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		public int getMinHeight() {
			return minHeight;
		}
		public void setMinHeight(int minHeight) {
			this.minHeight = minHeight;
		}
		public boolean isResizable() {
			return resizable;
		}
		public void setResizable(boolean resizable) {
			this.resizable = resizable;
		}
		public boolean isMaximizable() {
			return maximizable;
		}
		public void setMaximizable(boolean maximizable) {
			this.maximizable = maximizable;
		}
		public boolean isMinimizable() {
			return minimizable;
		}
		public void setMinimizable(boolean minimizable) {
			this.minimizable = minimizable;
		}
		public boolean isClosable() {
			return closable;
		}
		public void setClosable(boolean closable) {
			this.closable = closable;
		}
		public boolean isDraggable() {
			return draggable;
		}
		public void setDraggable(boolean draggable) {
			this.draggable = draggable;
		}

	}

	public Identification getIdentification() {
		return identification;
	}
	public void setIdentification(Identification identification) {
		this.identification = identification;
	}
	public Instantiation getInstantiation() {
		return instantiation;
	}
	public void setInstantiation(Instantiation instantiation) {
		this.instantiation = instantiation;
	}
	public Menu getMenu() {
		return menu;
	}
	public void setMenu(Menu menu) {
		this.menu = menu;
	}
	public Window getWindow() {
		return window;
	}
	public void setWindow(Window window) {
		this.window = window;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
}

