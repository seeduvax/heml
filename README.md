# HEML - Human Editable Markup Language
![alt text](_doc/src/hemllogo.png?raw=true "HEML Logo")

--
## What is HEML
HEML is yet another markup language designed to be shorter to write using a 
simple text editor. To do so it combines features from markdown and XML to 
reduce as much as possible the markup overhead.
This project is a HEML file processor to transform HEML files to XML, or any
other format by chaining XSLT.

A small example:
```
{section %title=paragraph layout example
paragraph
paragraph
- bullet
- bullet
	- sub bullet
}
```
The related XML output would be:
```
<section title="paragraph layout example">
	<p>paragraph</p>
	<p>paragraph</p>
	<li>bullet</li>
	<li>bullet</li>
	<ul>
		<li>bullet</li>
	</ul>
</section>
```

## Change management
Issue tracking is performed using the change request management feature from 
[AbcrobaticBuildSystem][0].

## Build
- Clone the project and checkout the branch or tag you need.
- Enter the project root directory (the on holdong the `app.cfg` file)
- Build the project
```
    make
```
- run tests
```
    make test
```

See [AcrobatomaticBuildSystem][0] documentation for more details about the build
features.

## Editors
HEML is used as the main documentation format for few projects managed with
[ABS][0], in particular my own projects (some are available on [github][1]). To
edit such document, a vim syntax highlight file is available. See also 
[fred322][2]'s [plugins][3] for VSCode and Eclipse.

[0]:https://github.com/seeduvax/AcrobatomaticBuildSystem
[1]:https://github.com/seeduvax?tab=repositories
[2]:https://github.com/fred322
[3]:https://github.com/fred322/hemleditor


