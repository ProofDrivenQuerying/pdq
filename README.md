![PDQ logo](https://www.cs.ox.ac.uk/projects/pdq/images/logo.png)

# Proof-Driven Query Planning

**PDQ (Proof-Driven Querying)** is a platform for generating _query plans_ over semantically-interconnected data sources with diverse access interfaces.

Check our [Wiki](https://github.com/michaelbenedikt/pdq/wiki) for more information.

## Installation and Usage

Please check our [Wiki](https://github.com/michaelbenedikt/pdq/wiki)

| 💻 [User Guides](https://github.com/michaelbenedikt/pdq/wiki/User-Guides) | 📃 [Development guidelines](https://github.com/michaelbenedikt/pdq/wiki/Development-guidelines) | 👩‍💻 [Developer Guides](https://github.com/michaelbenedikt/pdq/wiki/Developer-Guides) |
| :---------: | :---------: | :---------: |

## Contributing

[[TODO]] See [this issue](https://github.com/michaelbenedikt/pdq/issues/305)

## Credits

**[Information Systems Group](https://www.cs.ox.ac.uk/isg) - [Department of Computer Science](http://www.cs.ox.ac.uk) - [University of Oxford](www.ox.ac.uk)**

### Current members

- [Michael Benedikt](http://www.cs.ox.ac.uk/people/michael.benedikt/home.html)
- [Stefano Germano](https://www.cs.ox.ac.uk/people/stefano.germano)
- [Gabor Gyorkei](https://www.cs.ox.ac.uk/people/gabor.gyorkei)

<!-- - [Mark Ridler](https://www.cs.ox.ac.uk/people/mark.ridler) -->

### Former members

- [George Konstantinidis](https://www.cs.ox.ac.uk/people/george.konstantinidis) [2016-2017]
- [Julien Leblay](https://staff.aist.go.jp/julien.leblay) [2014-2015]
- [Efthymia Tsamoura](https://www.cs.ox.ac.uk/people/efthymia.tsamoura) [2014-2018]

<!-- - [Pierre Bourhis](https://www.cs.ox.ac.uk/people/pierre.bourhis) -->

## License

The source code is available for free for non-commercial use.
See the [LICENCE](https://github.com/michaelbenedikt/pdq/blob/master/LICENCE) file for details.

## What is in this distribution

The top level directory, containing this README, also contains 8 sub-directories each corresponding to a sub-project:

- `common`
  - Features the packages and classes used across the whole application
- `cost`
  - Plan cost estimation
- `datasources`
  - Web services and DBMS connector libraries
- `gui`
  - ???
- `planner`
  - Algorithm for planning purposes
- `reasoning`
  - Algorithm for reasoning purposes
- `regression`
  - Detecting regressions in the code
- `runtime`
  - Runtime execution of queries and plans
