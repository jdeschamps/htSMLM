# htSMLM

## A user interface for localization microscopes

htSMLM (high-throughput single-molecule localization microscopy) is an [EMU]( https://github.com/jdeschamps/EMU ) user interface for [Micro-Manager](https://micro-manager.org/wiki/Micro-Manager). EMU allows loading easily reconfigurable user interfaces in Micro-Manager. 

In particular, htSMLM features optional and reconfigurable controls for:

- Four lasers (on/off and laser percentage)
- One to four filterwheels (four to six positions each)
- Up to six on/off controls (servos, flip mirros)
- Focus monitoring, moving and locking (if a similar system to [pgfocus]( http://big.umassmed.edu/wiki/index.php/PgFocus ) is present)
- A quadrant photodiode monitoring (see pgfocus)
- Laser triggering interface for [MicroFPGA]( https://github.com/jdeschamps/MicroFPGA )
- Two iBeamSmart laser panels (focus-lock and booster tabs)

And in addition:

- A script panel to **automatically adjust the UV** power or pulse length to activate single-molecules.

- An acquisition panel for designing a set of experiments (localization, snapshot, z-stack...etc..) to be performed automatically, **enabling unsupervised localization microscopy**.

  

## Guide

To install and use htSMLM, please consult the [user guide](guide).



## Cite EMU and htSMLM
Deschamps, J., Ries, J. EMU: reconfigurable graphical user interfaces for Micro-Manager. BMC Bioinformatics 21, 456 (2020).
[doi.org/10.1186/s12859-020-03727-8](https://doi.org/10.1186/s12859-020-03727-8)


## Selected publications using htSMLM:

- Efficient homogeneous illumination and optical sectioning for quantitative single-molecule localization microscopy. 
  Deschamps J, Rowald A, Ries J. 
  *Opt Express* 24(24):28080-28090. doi: [10.1364/oe.24.028080](http://dx.doi.org/10.1364/oe.24.028080)

- Real-time 3D single-molecule localization using experimental point spread functions.Li Y, Mund M, Hoess P, Deschamps J, Matti U, Nijmeijer B, Sabinina VJ, Ellenberg J, Schoen I, Ries J.
  *Nat Methods* doi: [10.1038/nmeth.4661](http://dx.doi.org/10.1038/nmeth.4661)

- Systematic nanoscale analysis of endocytosis links efficient vesicle formation to patterned actin nucleation.
  Mund M, van der Beek JA, Deschamps J, Dmitrieff S, Hoess P, Monster JL, Picco A, Nédélec F, Kaksonen M, Ries J.
  *Cell* doi: [10.1016/j.cell.2018.06.032](http://dx.doi.org/10.1016/j.cell.2018.06.032)

- Depth-dependent PSF calibration and aberration correction for 3D single-molecule localization. 
  Li Y, Wu Y, Hoess P, Mund M, Ries J.
  *Biomedical Optics Express* doi: [10.1364/BOE.10.002708](http://dx.doi.org/10.1364/BOE.10.002708)

- A tessellation-based colocalization analysis approach for single-molecule localization microscopy.
  Levet F, Julien G, Galland R, Butler C, Beghin A, Chazeau A, Hoess P, Ries J, Giannone G, Sibarita JB.
  *Nature Communications* doi: [10.1038/s41467-019-10007-4](http://dx.doi.org/10.1038/s41467-019-10007-4)

- Nuclear pores as versatile reference standards for quantitative superresolution microscopy.
  Thevathasan JV, Kahnwald M, Cieśliński K, Hoess P, Peneti SK, Reitberger M, Heid D, Kasuba KC, Hoerner SJ, Li Y, Wu Y, Mund M, Matti U, Pereira PM, Henriques R, Nijmeijer B, Kueblbeck M, Sabinina VJ, Ellenberg J, Ries J.
  *Nature Methods* doi: [10.1038/s41592-019-0574-9](http://dx.doi.org/10.1038/s41592-019-0574-9)
  
- Cost-efficient open source laser engine for microscopy.
  Schröder D, Deschamps J, Dasgupta A, Matti U, Ries J.
  Biomed. Opt. Express 11, 609-623 (2020) 
  *Biomed Opt Express* doi: [10.1364/BOE.380815](https://doi.org/10.1364/BOE.380815)

- Optimizing imaging speed and excitation intensity for single-molecule localization microscopy.
  Diekmann, R., Kahnwald, M., Schoenit, A. et al. 
  Nat Methods 17, 909–912 (2020). 
  *Nature Methods* doi: [10.1038/s41592-020-0918-5](https://doi.org/10.1038/s41592-020-0918-5)

