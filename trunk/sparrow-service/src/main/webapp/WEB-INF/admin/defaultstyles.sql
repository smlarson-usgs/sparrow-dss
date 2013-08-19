Rem
Rem $Header: defaultstyles.sql 16-sep-2005.10:54:15 lqian Exp $
Rem
Rem defaultstyles.sql
Rem
Rem Copyright (c) 2001, 2005, Oracle. All rights reserved.  
Rem
Rem    NAME
Rem      defaultstyles.sql - populate default styles metadata
Rem
Rem    DESCRIPTION
Rem      Default styles metadata for MDSYS
Rem
Rem    NOTES
Rem      <other useful comments, qualifications, etc.>
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem    lqian       09/16/05 - lqian_reorg_ear_files
Rem    lqian       03/25/03 - 
Rem    lqian       03/14/03 - add more styles for OBE
Rem    lqian       02/28/03 - add commit
Rem    cjmurray    07/02/02 - Edits to spelling and wording of descriptions
Rem    pfwang      08/23/01 - Merged pfwang_dir_change
Rem    pfwang      08/21/01 - Created
Rem

SET ECHO ON
SET FEEDBACK 1
SET NUMWIDTH 10
SET LINESIZE 80
SET TRIMSPOOL ON
SET TAB OFF
SET PAGESIZE 100


-- Populate basic styles
--
-- Color

insert into user_sdo_styles (name, type, description, definition) values (
'C.ROSY BROWN STROKE',             'COLOR',
'Rosy brown stroke',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="stroke:#bc8f8f">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.CHARCOAL W/ ROSY BROWN BORDER',                     'COLOR',
'Charcoal (gray-black) with rosy brown border',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="stroke:#bc8f8f;fill:#808080">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.RED W/ BLACK BORDER',            'COLOR',
'Red (slight orange) with black border',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="stroke:#000000;fill:#ee1100">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.RED',                            'COLOR',
'Red',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="fill:#ff1100">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.BLUE',                           'COLOR',
'Blue',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="fill:#0000ff">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.YELLOW',                         'COLOR',
'Yellow',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="fill:#ffff00">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.WATER',                          'COLOR',
'Aqua blue (color for rendering water)',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="fill:#a6caf0">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.WHITE',                          'COLOR',
'White',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="stroke:#ffffff">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.BLACK',                          'COLOR',
'Black',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="fill:#000000">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.US MAP YELLOW',                  'COLOR',
'Yellow main color for US maps',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="stroke:#bb99bb;fill:#ffffcc">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.ROSY BROWN',                     'COLOR',
'Rosy brown',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="fill:#bc8f8f">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.SANDY BROWN',                    'COLOR',
'Sandy (yellowish) brown',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="fill:#f4a460">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.WHEAT',                          'COLOR',
'Wheat',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="fill:#f5deb3">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.PARK FOREST',                    'COLOR',
'Green for park or forest',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="fill:#adcda3">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.LIGHT YELLOW W/ GRAY BORDER',                   'COLOR',
'Light yellow (for map background) with gray border',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="stroke:#aaaaaa;fill:#ffffce">
<rect width="50" height="50"/></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'C.COUNTIES',                   'COLOR',
null,
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="color" style="stroke:#003333;fill:#ffffcc">
<rect width="50" height="50"/></g>
</svg>');


-- Marker

insert into user_sdo_styles (name, type, description, definition) values (
'M.REDSQ',                          'MARKER',
'Square (red wih black border)',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="marker"  style="stroke:#000000;fill:#ff0000">
<polygon points="0.0,0.0, 0.0,100.0, 100.0,100.0, 100.0,0.0, 0.0,0.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'M.STAR',                           'MARKER',
'Star (red with black border)',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="marker"  style="stroke:#000000;fill:#ff0000;width:15;height:15">
<polygon points="138.0,123.0, 161.0,198.0, 100.0,152.0, 38.0,198.0, 61.0,123.0,
0.0,76.0, 76.0,76.0, 100.0,0.0, 123.0,76.0, 199.0,76.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'M.CIRCLE',                         'MARKER',
'Circle (red with blue border)',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="marker"  style="stroke:#0000ff;fill:#ff0000">
<circle cx="0" cy="0" r="40.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'M.TRIANGLE',                       'MARKER',
'Triangle (medium blue with blue border',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="marker"  style="stroke:#0000ff;fill:#6666ff">
<polygon points="201.0,200.0, 0.0,200.0, 101.0,0.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'M.PENTAGON',                       'MARKER',
'Pentagon (yellow with blue border)',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="marker"  style="stroke:#0000ff;fill:#ffff00">
<polygon points="38.0,199.0, 0.0,77.0, 100.0,1.0, 200.0,77.0, 162.0,199.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'M.HEXAGON',                        'MARKER',
'Hexagon (red with black border)',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="marker"  style="stroke:#000000;fill:#ff0000">
<polygon points="50.0,199.0, 0.0,100.0, 50.0,1.0, 149.0,1.0, 199.0,100.0, 149.0,199.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'M.HOUSE',                         'MARKER',
'Simple house',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="marker"  style="stroke:#000000;fill:#666666;width:19;height:15">
<polygon points="20.0,10.0, 25.0,10.0, 30.0,6.0, 36.0,10.0, 40.0,10.0, 
40.0,19.0, 20.0,19.0, 20.0,10.0" />
</g>
</svg>');

commit;
-- Line

insert into user_sdo_styles (name, type, description, definition) values (
'L.RAILROAD',                       'LINE',
'Railroad',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="line" style="fill:#003333;stroke-width:1">
<line class="hashmark" style="fill:#003333"  dash="8.5,3.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'L.TOLL',                           'LINE',
'Primary toll highway',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="line" style="fill:#66ff66;stroke-width:4">
<line class="parallel" style="fill:#66ff33;stroke-width:1.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'L.RAMP',                           'LINE',
'Ramp (highway entrance or exit)',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="line" style="fill:#ffc800;stroke-width:2">
<line class="base" style="fill:#998899;stroke-width:2.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'L.MAJOR TOLL ROAD',                'LINE',
'Major toll road',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="line" style="fill:#006600;stroke-width:4;stroke-linecap:SQUARE">
<line class="parallel" style="fill:#99ffff;stroke-width:1.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'L.FERRY',                          'LINE',
'Ferry line',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="line" style="stroke-width:1">
<line class="base" style="fill:#000066;stroke-width:1.0" dash="5.0,3.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'L.DPH',                            'LINE',
'Divided primary highway',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="line" style="fill:#ffff00;stroke-width:5">
<line class="parallel" style="fill:#ff0000;stroke-width:1.0" />
<line class="base" style="fill:black;stroke-width:1.0" dash="10.0,4.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'L.SH',                             'LINE',
'Secondary highway',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="line" style="fill:#ffc800;stroke-width:2">
<line class="base" style="fill:#998899;stroke-width:2.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'L.PH',                             'LINE',
'Primary highway',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="line" style="fill:#33a9ff;stroke-width:4">
<line class="parallel" style="fill:#aa55cc;stroke-width:1.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'L.LIGHT DUTY',                     'LINE',
'Light duty road',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="line" style="fill:#404040;stroke-width:2">
<line class="base" /></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'L.STREET',                         'LINE',
'Street',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="line" style="fill:#a0a0a0;stroke-width:1">
<line class="base" /></g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'L.STATE BOUNDARY',                 'LINE',
'State boundary',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="line" style="fill:#bb99bb;stroke-width:5">
<line class="base" style="fill:#0000ff;stroke-width:1.0" dash="8.0,4.0" />
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'L.MAJOR STREET',                   'LINE',
'Major street',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="line" style="fill:#ff4040;stroke-width:2">
<line class="base" /></g>
</svg>');

commit;
-- Text

insert into user_sdo_styles (name, type, description, definition) values (
'T.TITLE',                          'TEXT',
'Map title',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="text" style="font-style:plain;font-family:SansSerif;font-size:18pt;font-weight:bold;fill:#0000ff">
 Hello World!
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'T.ROAD NAME',                     'TEXT',
'Road name',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="text" style="font-style:plain;font-family:Serif;font-size:11pt;font-weight:bold;fill:#000000">
 Hello World!
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'T.CITY NAME',                      'TEXT',
'City name',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="text" style="font-style:plain;font-family:Dialog;font-size:12pt;font-weight:bold;fill:#000000">
 Hello World!
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'T.STATE NAME',                     'TEXT',
'State name',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="text" style="font-style:plain;font-family:Dialog;font-size:14pt;font-weight:bold;fill:#0000ff">
 Hello World!
</g>
</svg>');

insert into user_sdo_styles (name, type, description, definition) values (
'T.STREET NAME',                    'TEXT',
'Street name',
'<?xml version="1.0" standalone="yes"?>
<svg width="1in" height="1in">
<desc></desc>
<g class="text" style="font-style:plain;font-family:Dialog;font-size:10pt;fill:#0000ff">
 Hello World!
</g>
</svg>');

commit;
-- Advanced styles

insert into user_sdo_styles (name, type, description, definition) values (
'V.COLOR SERIES 1',                    'ADVANCED',
'A sample color scheme',
'<?xml version="1.0" ?>
<AdvancedStyle>
  <ColorSchemeStyle basecolor="#ffff00" strokecolor="#00aaaa">
     <Buckets low="0.0" high="20000.0" nbuckets="10" />
  </ColorSchemeStyle>
</AdvancedStyle>');

insert into user_sdo_styles (name, type, description, definition) values (
'V.COLOR SERIES 2',                    'ADVANCED',
'A sample color scheme.',
'<?xml version="1.0" ?>
<AdvancedStyle>
  <ColorSchemeStyle basecolor="#ff0000" strokecolor="#000000">
     <Buckets low="0.0" high="100.0" nbuckets="6" />
  </ColorSchemeStyle>
</AdvancedStyle>');

insert into user_sdo_styles (name, type, description, definition) values (
'V.CIRCLES',                    'ADVANCED',
'A sample circle series.',
'<?xml version="1.0" ?>
<AdvancedStyle>
  <VariableMarkerStyle basemarker="MDSYS:M.CIRCLE" startsize="7" increment="4" >
    <Buckets>
      <RangedBucket seq="0" label="less than 4" high="4.0" />
      <RangedBucket seq="1" label="4 - 5" low="4.0" high="5.0" />
      <RangedBucket seq="2" label="5 - 6" low="5.0" high="6.0" />
      <RangedBucket seq="3" label="6 - 7" low="6.0" high="7.0" />
      <RangedBucket seq="4" label="7 and up" low="7.0" />
    </Buckets>
  </VariableMarkerStyle>
</AdvancedStyle>');

commit;

