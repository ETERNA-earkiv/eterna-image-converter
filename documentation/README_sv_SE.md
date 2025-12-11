# Bildkonvertering - Användarhandledning

## Översikt

Bildkonverteringsjobbet är utformat för att stödja digitala bevarandearbetsflöden i ETERNA-systemet. Verktyget konverterar olika bildformat till bevarandevänliga format för att säkerställa långsiktig tillgång och arkivbeständighet för digitala bilder.

## Så här använder du bildkonverteringensjobbet

1. **Starta konvertering**: Du kan påbörja en konverteringsprocess genom att välja en Logisk enhet, en Representation eller en enskild fil via Katalog sidan eller Sök sidan och starta ett nytt Arkivvårdsjobb.
2. **Välj utdataformat**: Välj önskat utdataformat från tillgängliga alternativ (JPG, PNG eller TIFF)
3. **Konfigurera konvertering**: Ange om du vill skapa en ny representation eller disseminationskopia
4. **Kör konvertering**: Kör verktyget för att starta konverteringsprocessen

### Format som stöds som indata
Verktyget stöder konvertering från ett brett utbud av bildformat inklusive:
- **Vanliga format**: BMP, PNG, TIFF, JPEG, GIF, PSD
- **Äldre format**: PNM, PICT, ICO, CUR, DDS, HDR, TGA, PCX, DCX
- **Vektorgrafik**: SVG och SVGZ (konverteras med specialiserad bearbetning)
- **m.fl**

### Utdataformat
Du kan konvertera bilder till tre bevarandeformat:

- **JPG**: Förlustkomprimerande format lämpligt för fotografier där viss kvalitetsförlust accepteras för mindre filstorlekar
- **PNG**: Förlustfritt komprimerande format idealiskt för bilder med skarpa linjer, text och grafik
- **TIFF**: Förlustfritt format perfekt för arkivering, bevarar maximal kvalitet och stöder metadata

### Konverteringsprocess
1. **Formatdetektering**: Verktyget detekterar automatiskt indataformatet
2. **Bildnormalisering**: Bilder justeras automatiskt för målformatet, inklusive hantering av transparens, färgrymder och bitdjup efter behov
3. **Kvalitetsbedömning**: Filer utvärderas för att säkerställa att minimal kvalitetsförlust sker under konvertering
4. **Konverteringskörning**: Bilder konverteras med specialiserade bibliotek för optimala resultat
5. **Skapande av representation**: Vid konvertering kommer de nya filerna placeras i samma logiska enhet som originalen i en representation med vald representationstyp och status: `Bevarande`. Om ingen sådan representation redan finns i den logiska enheten kommer en ny skapas.

## Kända begränsningar

### Anteckningar om filhantering
- Filer som redan är i målformatet konverteras inte
- Om flera filer har samma namn kan endast en bevaras på grund av systembegränsningar

### Kvalitetsinställningar
- SVG till JPG-konverteringar använder 95% kvalitetsinställning för att balansera filstorlek och visuell trohet
- Bilder med transparens blandas automatiskt med vit bakgrund vid konvertering till JPG-format
- Färgrymder konverteras automatiskt till sRGB-standard när det behövs för kompatibilitet
- Bitdjup justeras för att matcha formatkrav (t.ex. minskning av hög-bitdjup-bilder för JPG-kompatibilitet)
- Andra konverteringar bibehåller högsta möjliga kvalitet för det valda formatet

## Bästa praxis

1. **Välj lämpliga format**: Välj TIFF för maximal arkivkvalitet, PNG för förlustfri komprimering, JPG endast när filstorlek är kritisk
2. **Använd unika filnamn**: Se till att filer har unika namn för att undvika potentiella överskrivningar
3. **Testa konverteringar**: Kör testkonverteringar på samplefiler innan du bearbetar stora samlingar
4. **Granska resultat**: Verifiera alltid att de konverterade filerna uppfyller dina bevarandekrav
5. **Bevara original**: Behåll originalfiler tillgängliga tillsammans med konverterade versioner

## Felsökning

### Vanliga problem

**Konvertering misslyckas**
- **Orsak**: Indataformat stöds inte eller filen är skadad
- **Lösning**: Verifiera att filformatet stöds och kontrollera filintegritet

**Filer inte konverterade**
- **Orsak**: Filen är redan i målformat eller i exkluderad kategori
- **Lösning**: Kontrollera om konvertering verkligen behövs

**Oväntade resultat**
- **Transparenta bilder**: Alfakanaler blandas med vit bakgrund vid JPG-konvertering
- **Hög bitdjup-bilder**: Justeras automatiskt för att matcha målformatets kapacitet
- **Färgvariationer**: Bilder konverteras till standard sRGB-färgrymd när det behövs

**Prestandaproblem**
- **Stora filstorlekar**: TIFF-format bevarar maximal kvalitet men skapar större filer
- **Långsam bearbetning**: Bilder med högt bitdjup - eller komplexa bilder tar längre tid att bearbeta

### Ytterligare anteckningar

- **Formatval**: Välj lämpligt utdataformat för din innehållstyp (TIFF för arkivering, PNG för grafik, JPG för foton)
- **Format som inte stöds**: Vissa filformat stöds inte och kan inte konverteras

För teknisk support eller frågor om specifika format, kontakta din systemadministratör eller skapa ett ärende på vårt GitHub-arkiv.
