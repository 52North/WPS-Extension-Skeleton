package org.n52.wps.testbed12.module;

import java.util.Arrays;
import java.util.List;

import org.n52.wps.testbed12.DummyAlgorithmRepository;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ClassKnowingModule;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationKey;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.FormatEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;

public class Testbed12ProcessConfigModule extends ClassKnowingModule {

	private boolean active = true;

	public static final String hootenannyHomeKey = "hootenanny_home";
	
	private ConfigurationEntry<String> hootenannyHomeEntry = new StringConfigurationEntry(hootenannyHomeKey, "Hootenanny home", "Path to Hootenanny installation",
			true, "/usr/local/hootenanny-0.2.24/");	
	
	private List<? extends ConfigurationEntry<?>> configurationEntries = Arrays.asList(hootenannyHomeEntry);

	private String hootenannyHome;
	
	@Override
	public String getModuleName() {
		return "Testbed-12 processes config module.";
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public ConfigurationCategory getCategory() {
		return ConfigurationCategory.REPOSITORY;
	}

	@Override
	public List<? extends ConfigurationEntry<?>> getConfigurationEntries() {
		return configurationEntries;
	}

	@Override
	public List<AlgorithmEntry> getAlgorithmEntries() {
		return null;//TODO, can we show the algorithms here somehow?
	}

	@Override
	public List<FormatEntry> getFormatEntries() {
		return null;
	}

	public String getHootenannyHome() {
		return hootenannyHome;
	}

	@ConfigurationKey(key = hootenannyHomeKey)
	public void setHootenannyHome(String hootenannyHome) {
		this.hootenannyHome = hootenannyHome;
	}

	@Override
	public String getClassName() {
		return DummyAlgorithmRepository.class.getName();
	}

}
