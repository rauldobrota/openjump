--- Informations about the OpenJUMP PostGIS PlugIn (2006-07-29)

The JumpPostGIS PlugIn is an extension of the original
PostGIS driver created by Refractions Research (www.refractions.net) for Jump.

With this driver you can read and write PostgreSQL/PostGIS tables
with OpenJUMP.

If this plugIn is in the OpenJUMP plugIn-directory lib/ext ,
you can read tables with File>Load Dataset(s) and write tables with
File>Save dataset as... , if you choose 
"PostGIS Table" in "Format:" (ComboBox left up).

Attention! Please use only lower case column names!

You can choose between "New Table", "Overwrite"" and "Insert" mode:


- New Table

  If the table does not exist, it will created.
  If the table does exist, it will deleted and created.
  
- Overwrite

  If the table does not exist, it will created.
  If the table does exist, only the data will deleted (not the whole table)
  and the new data will inserted. The CONSTRAINTs of the table will not changed.
  
  
- Insert

  If the table does exist, only the rows with the same unique column-value
  will updated. Other rows will inserted into the existing dataset.
  So it is better, that the "Unique Column" contains the primary-key of the
  dataset.

If you have questions, please mail me:

Uwe Dalluege

uwe.dalluege@rzcn.haw-hamburg.de

HCU Hamburg, Department Geomatic

http://www.hcu-hamburg.de/geomatik/ 






