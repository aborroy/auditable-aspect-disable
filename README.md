# Disable AUDITABLE ASPECT Behaviour when adding an Aspect

**Description**

Disable AUDITABLE Behaviour when adding an aspect `triggeringAspectQName`

`triggeringAspectQName` can be specified at `alfresco-global.properties`, by default:

```
triggering.aspect.qname={http://www.alfresco.org/model/content/1.0}templatable
```

**Usage**

By default, when a change is performed on a node having AUDITABLE ASPECT, Alfresco updates `cm:modified` and `cm:modifier` properties.
When using this module, these properties remain unchanged after adding the ASPECT specified in `alfresco-global.properties`

**License**

The module is licensed under the [LGPL v3.0](http://www.gnu.org/licenses/lgpl-3.0.html).

**State**

Current release is 1.0.0

**Compatibility**

The current version has been developed using Alfresco Community 6.1 and Alfresco SDK 4.0.0

## Building

```
$ mvn clean package

$ $ ls target/*.jar
target/auditable-aspect-disable-1.0.0.jar
```
## Deploying

Copy `auditable-aspect-disable-1.0.0.jar` to `platform/modules` folder in your Alfresco Repository installation and re-start the service.
