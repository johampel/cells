# Cells

`Cells` is a cell simulation game, originally based on Conways game of Life.
Unlike Conways Game of Life, which has a fixed number of cell types and fixed rules describing how the cell types 
interact, `Cells` allows to define cell cultures with up to 255 different cell types and also allows to define own 
rules.


## Building

Simple build:
~~~
mvn clean install
~~~

Building an executable image using `jpackage`:
~~~
mvn clean install -Pimage
~~~
Note that building an executable is tested for MacOs only yet.


## Contributing

Is *very* welcome. I am interested in:

- Suggestions for improvements
- PRs (e.g. for the "Open Issues" mentioned below)
- Feedback
- etc


# Open issues
- Localization for further languages.
- There are smaller bugs in it, but as far as I can see no showstopper
- Improving UX by more support of shortcuts
