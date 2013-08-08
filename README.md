# 52°North WPS Skeleton Project

You can use this project skeleton to develop WPS extension
Processes (Algorithms), Parsers and Generators.

More information available at the [52°North Geoprocessing Community](http://52north.org/geoprocessing).

## Adding an Algorithm to your WPS instance

There are two steps which you need to fulfill

1. Copy the resulting jar to (WPS-deployment-directory)/WEB-INF/lib
2. Add your new Algorithms to the WPS config (in (WPS-deployment-directory)/config/wps_config.xml)

The second step could look like:

```xml
...
<Repository name="LocalAlgorithmRepository"
	className="org.n52.wps.server.LocalAlgorithmRepository" active="true">
	<Property name="Algorithm" active="true">org.n52.wps.extension.ExtensionAlgorithm</Property>
	<Property name="Algorithm" active="true">org.n52.wps.extension.AnnotatedExtensionAlgorithm</Property>
	<Property name="Algorithm" active="true">org.n52.wps.server.algorithm.SimpleBufferAlgorithm</Property>
  ...
</Repository>
...
```

### Only if you are using WPS 3.2.0 and later

If you add the fully qualified name of you algorithm to the file:

/META-INF/services/org.n52.wps.server.IAlgorithm

and drop the jar to (WPS-deployment-directory)/WEB-INF/lib, your Algorithm will be added to the WPS automatically.