# MC_Champions_1.21.1_Tool_CSV_To_Config_Generator
A simple Java tool for the Champions mod that generates configurations given a CSV file. I recommend using another program like Google Sheets alongside this app to manage your Champions configs more easily.

CSV file is in resources. It's only using Java, Maven, and Lombok so use any IDE that supports all three (like intelliJ) and you're golden. Put the CSV in resources (it comes with an example; starts pulling data at row 6+ to give room for headers, math based fields etc.)

# Generated files given CSV
Outside the project folder after generating, you'll find:

config/champions_entities - listing individual filtered affixes etc. per allowed mob.

data\champions\tags\entity_type/allow_champions.json - The list of allowed champions. (First column of CSV is the mob ID) Intended to be put into a data pack. (See: [Creating a Datapack](https://minecraft.wiki/w/Tutorial:Creating_a_data_pack))

# Overrides in code
Supports overriding max rank for specific mobs. By default this is Silverfish and a couple other possible on-death spawns, and some summons like a Vex limited to rank 2 maximum. These will not impact the list itself; only the max rank specific IDs will map to, essentially overriding them through code rather than needing any "hard coding" in the spreadsheet. I recommend all math-based max ranks on the spreadsheet.

[Example Spreadsheet Here](https://docs.google.com/spreadsheets/d/1Rv5-hfRvJ1aRjfpPCm2gPq7oFlsTEBoSP9QgvW6e98w/edit?usp=drivesdk)
(File -> Make a Copy)
