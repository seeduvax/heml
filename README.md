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

See [AcrobatomaticBuildSystem][0] documentation for more details about the build features.

[0]:https://github.com/seeduvax/AcrobatomaticBuildSystem

