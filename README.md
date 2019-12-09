# RedmiK20toMi9T
Automatizált "*Redmi K20*" -> "*Mi 9T*" átnevezés a *Xiaomi Mi 9T*-n olyan ROM-ot használóknak, ahol a készülék típusa "Redmi K20", ilyen pl. a *xiaomi.eu* ROM.

## használat:
Az alkalmazás több dolgot is elvégez, amik egymástól függetlenek és egyenkén ki/be kapcsolhatók.

Először is meg lehet adni az új elnevezést, "*Mi 9T*" van alapból beállítva,  de ez átírható.
- **build.prop**: megkeresi a /system és minden alkönyvtára alatt azokat a text-fájlokat, ahol "*Redmi K20*" előfordul és kicseréli a megadott elnevezésre, de előtte (ha még nem létezett) egy mentést csinál a fájl nevéhez a "`.save`" hozzábiggyesztésével. A "`.save`" végű fájlokat nem bántja. Az átnevezés többször is lehetséges, mert ha már létezik mentés, akkor azt használja.
- **bootlogo**: kicseréli a Redmi-s bootlogót az eredeti Mi bootlogóra (`/dev/block/sde46`)
- **bootanimation**: kicseréli a Redmi-s bootanimációt az eredeti Mi bootanimációra (`/system/media/bootanimation.zip`)
- **watermark**: kicseréli a kamera "*Redmi K20*"-as vízjelét az eredeti "*Mi 9T*"-s vízjelre. Az egyéni vízjel alapértelmezésben "*AI TRIPLE CAMERA*", ha mást szeretnénk, akkor azt a kamerabeállítások -> vízjel -> egyéni vízjel alatt megtehetjük.
- **/bin add ll, rw, ro**: a cseréhez nincs köze, csak 
  + az `ls -l` parancsot `ll`-re rövidíti,
  + a `mount -o remount -rw` parancsot `rw`-re rövidíti,
  + a `mount -o remount -r` parancsot `ro`-ra rövidíti

Az alkalmazás töbször is futtatható, a bootlogo, bootanimáció és vízjel esetében ha már egyszer megtörtént a csere, azt többször már nem ajánlja fel.

Az alkalmazás root jogosultságot kér, más jogosultság nem is kell neki. Az alkalmazás root alatt kiadott parancsokat hajt végre, minden parancs protokollálva van, egyes parancsok eredménye kékkel, ha hiba történne az a parancs pirossal. A `grep` kb. fél percig tart, a többi már gyorsan megy. A végére kell görgetni, ott remélhetőleg egy zöld sor látszik majd.

Természetesen csak saját felelősségre használni, előtte feltétlen egy backup-ot csinálni.

Ez a Mi 9T-re jó, a PRO esetén a bootlogo nem javallott, mert nem tudom, hogy ott is a `/dev/block/sde46` partíció a bootlogo, igaz, hogy a partíció hosszát ellenőrzöm és csak akkor írom felül, ha megegyezik a bootlogo fájl hosszával. A többi pont a PRO esetén is megy.
