# Bildkonvertering - Användarhandledning

## Översikt

Bildkonverteringsjobbet är utformat för att stödja digitala bevarandearbetsflöden i ETERNA-systemet. Verktyget konverterar olika bildformat till bevarandevänliga format för att säkerställa långsiktig tillgång och arkivbeständighet för digitala bilder.

## Så här använder du bildkonverteringensjobbet

1. **Starta konvertering**: Du kan påbörja en konverteringsprocess genom att välja en Logisk enhet, en Representation eller en enskild fil via Katalog sidan eller Sök sidan och starta ett nytt Arkivvårdsjobb.
3. **Välj utdataformat**: Välj önskat utdataformat från tillgängliga alternativ (JPG, PNG eller TIFF)
4. **Konfigurera konvertering**: Ange om du vill skapa en ny representation eller disseminationskopia
5. **Kör konvertering**: Kör verktyget för att starta konverteringsprocessen

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
2. **Kvalitetsbedömning**: Filer utvärderas för att säkerställa att ingen kvalitetsförlust sker under konvertering
3. **Konverteringskörning**: Bilder konverteras med specialiserade bibliotek för optimala resultat
4. **Skapande av representation**: Vid konvertering kommer de nya filerna placeras i samma logiska enhet som originalen i en representation med vald representationstyp och status: `Bevarande`. Om ingen sådan representation redan finns i den logiska enheten kommer en ny skapas.

## Kända begränsningar

### Anteckningar om filhantering
- Filer som redan är i målformatet konverteras inte
- Om flera filer har samma namn kan endast en bevaras på grund av systembegränsningar

### Kvalitetsinställningar
- SVG till JPG-konverteringar använder 95% kvalitetsinställning för att balansera filstorlek och visuell trohet
- Andra konverteringar bibehåller högsta möjliga kvalitet för det valda formatet

## Bästa praxis

1. **Välj lämpliga format**: Välj TIFF för maximal arkivkvalitet, PNG för förlustfri komprimering, JPG endast när filstorlek är kritisk
2. **Använd unika filnamn**: Se till att filer har unika namn för att undvika potentiella överskrivningar
3. **Testa konverteringar**: Kör testkonverteringar på samplefiler innan du bearbetar stora samlingar
4. **Granska resultat**: Verifiera alltid att de konverterade filerna uppfyller dina bevarandekrav
5. **Bevara original**: Behåll originalfiler tillgängliga tillsammans med konverterade versioner

## Felsökning

- **Konvertering misslyckas**: Kontrollera att indataformatet stöds och att filen inte är skadad.  
    Andra möjliga orsaker som ger kvalitetsförsämring kan vara:
    - **Transparenta bilder**: Filer med alfakanaler (transparens) vid konvertering till JPG-format
    - **Hög bitdjup**: Bilder med bitdjup högre än det som målformatet stöder
    - **Animerade format**: Animerade bilder (som GIF) för att bevara animationsbildrutor
    - **Format som inte stöds**: Filer med filändelser som är kända för att orsaka problem (cur, pict, ico, dds, pfm, hdr)
- **Filer inte konverterade**: Verifiera att filen inte är i en exkluderad kategori eller redan i målformatet
- **Kvalitetsproblem**: Säkerställ att du använder ett lämpligt utdataformat för din innehållstyp
- **Stora filstorlekar**: TIFF-format kommer att producera större filer men bevarar maximal kvalitet

För teknisk support eller frågor om specifika format, kontakta din systemadministratör.
