package com.teliacompany.springfield.addressmaster.model;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
// Sources: https://diva.teliacompany.net/confluence/display/ONEVIEW/AddressMaster+Response+Code+Handling & bundled AddressMaster Interface in Service Catalog_PA6.docx
public enum ResponseCode {
    CODE_0(0, HttpStatus.OK, "OK"),
    CODE_10(10, HttpStatus.BAD_REQUEST, "Punkt-id ej numeriskt"),
    CODE_11(11, HttpStatus.UNAUTHORIZED, "Anropande system ej behörigt"),
    CODE_12(12, HttpStatus.NOT_FOUND, "Punkt-id saknas i AM"),
    CODE_13(13, HttpStatus.BAD_REQUEST, "Postort maste anges (Max 50 tkn)"),
    CODE_14(14, HttpStatus.NOT_FOUND, "Postort saknas i AM"),
    CODE_15(15, HttpStatus.NOT_FOUND, "Gatunamn saknas i AM"),
    CODE_16(16, HttpStatus.NOT_FOUND, "Gatunummer saknas i AM"),
    CODE_17(17, HttpStatus.NOT_FOUND, "Ingång saknas i AM"),
    CODE_18(18, HttpStatus.NOT_FOUND, "Uppgång saknas i AM (eller överliggande nivå i anropet)"),
    CODE_19(19, HttpStatus.NOT_FOUND, "Trappantal saknas i AM (eller överliggande nivå i anropet)"),
    CODE_20(20, HttpStatus.NOT_FOUND, "TV/TH saknas i AM (eller överliggande nivå i anropet)"),
    CODE_21(21, HttpStatus.NOT_FOUND, "Lägenhetsnummer saknas i AM (eller överliggande nivå i anropet)"),
    CODE_22(22, HttpStatus.PAYLOAD_TOO_LARGE, "Det finns fler än 50 adresser på närmast underliggande nivå"),
    CODE_23(23, HttpStatus.BAD_REQUEST, "Ogiltigt gatunummer"),
    CODE_24(24, HttpStatus.BAD_REQUEST, "Ogiltig ingång"),
    CODE_25(25, HttpStatus.BAD_REQUEST, "Ogiltig uppgång"),
    CODE_26(26, HttpStatus.BAD_REQUEST, "Ogiltigt trappantal"),
    CODE_27(27, HttpStatus.BAD_REQUEST, "Ogiltigt TV/TH"),
    CODE_28(28, HttpStatus.NOT_FOUND, "Postnummer saknas i AM"),
    CODE_29(29, HttpStatus.PAYLOAD_TOO_LARGE, "Flera gator med samma namn finns inom angiven postort (postnummer måste anges)"),
    CODE_30(30, HttpStatus.BAD_REQUEST, "Postnummer måste anges vid uppläggning av nytt gatunummer"),
    CODE_31(31, HttpStatus.BAD_REQUEST, "Gatunamn måste anges (max 50 tkn)"),
    CODE_32(32, HttpStatus.BAD_REQUEST, "Gatunummer måste anges"),
    CODE_33(33, HttpStatus.BAD_REQUEST, "Ingång måste anges"),
    CODE_34(34, HttpStatus.BAD_REQUEST, "Uppgång måste anges"),
    CODE_35(35, HttpStatus.BAD_REQUEST, "Trappantal måste anges"),
    CODE_36(36, HttpStatus.BAD_REQUEST, "TV/TH måste anges"),
    CODE_37(37, HttpStatus.BAD_REQUEST, "Postnummer måste anges vid uppläggning av ny gata (en uppläggning per postnummer om gatan finns på flera postnummer)"),
    CODE_38(38, HttpStatus.BAD_REQUEST, "Ogiltigt anropande system-id"),
    CODE_39(39, HttpStatus.BAD_REQUEST, "Ogiltigt Postnummer"),
    CODE_40(40, HttpStatus.BAD_REQUEST, "Ogiltigt lägenhetsnummer"),
    CODE_41(41, HttpStatus.CONFLICT, "Adressen finns redan registrerad i AM"),
    CODE_42(42, HttpStatus.PAYLOAD_TOO_LARGE, "Det finns fler än 100 adresser som motsvarar sökvillkoren"),
    CODE_43(43, HttpStatus.BAD_REQUEST, "Ogiltig koordinattyp"),
    CODE_44(44, HttpStatus.BAD_REQUEST, "Ogiltig landskod"),
    CODE_45(45, HttpStatus.BAD_REQUEST, "Ogiltigt koordinatsystem"),
    CODE_46(46, HttpStatus.BAD_REQUEST, "Orimligt värde på koordinater"),
    CODE_47(47, HttpStatus.BAD_REQUEST, "Fält får ej vara tomt"),
    CODE_48(48, HttpStatus.CONFLICT, "Denna landskod finns redan"),
    CODE_49(49, HttpStatus.CONFLICT, "Denna returkod finns redan"),
    CODE_50(50, HttpStatus.NOT_FOUND, "Landskod saknas i AM"),
    CODE_51(51, HttpStatus.BAD_REQUEST, "Nytt Alias är ej angivet"),
    CODE_52(52, HttpStatus.OK, "Nytt Alias är nu registrerat!"),
    CODE_53(53, HttpStatus.CONFLICT, "Alias är redan registrerat!"),
    CODE_54(54, HttpStatus.BAD_REQUEST, "Ogiltigt antal adresser"),
    CODE_55(55, HttpStatus.NOT_FOUND, "Läns- och kommunkod saknas i AM"),
    CODE_56(56, HttpStatus.NOT_FOUND, "Församlingskod saknas i AM"),
    CODE_57(57, HttpStatus.PAYLOAD_TOO_LARGE, "Svaret innehåller för många träffar. Precisera sökvillkoren!"),
    CODE_58(58, HttpStatus.BAD_REQUEST, "Ogiltig länskod"),
    CODE_59(59, HttpStatus.BAD_REQUEST, "Ogiltig kommunkod"),
    CODE_60(60, HttpStatus.BAD_REQUEST, "Ogiltig församlingskod"),
    CODE_61(61, HttpStatus.NOT_FOUND, "Länskod saknas i AM"),
    CODE_62(62, HttpStatus.NOT_FOUND, "Kommunkod saknas i AM"),
    CODE_63(63, HttpStatus.NOT_FOUND, "Församlingskod saknas i AM"),
    CODE_64(64, HttpStatus.NOT_FOUND, "Adressen uppdaterades i AM"),
    CODE_65(65, HttpStatus.BAD_REQUEST, "Fastighet ofullständigt ifyllt"),
    CODE_66(66, HttpStatus.NOT_FOUND, "Fastigheten saknas i AM"),
    CODE_67(67, HttpStatus.NOT_FOUND, "Sökta uppgifter saknas i AM"),
    CODE_68(68, HttpStatus.NOT_FOUND, "Koordinater saknas i AM"),
    CODE_69(69, HttpStatus.NOT_FOUND, "Adress saknas på fastigheten"),
    CODE_70(70, HttpStatus.NOT_FOUND, "Kan inte hitta två närliggande gatunummer"),
    CODE_71(71, HttpStatus.NOT_FOUND, "Station saknas i AM"),
    CODE_72(72, HttpStatus.NOT_FOUND, "Ingen site finns för angivna sökvilkor"),
    CODE_73(73, HttpStatus.NOT_FOUND, "Siteadress måste ha värde Y eller N"),
    CODE_74(74, HttpStatus.BAD_REQUEST, "Kommun måste ha ett värde"),
    CODE_75(75, HttpStatus.BAD_REQUEST, "Trakt måste ha ett värde"),
    CODE_76(76, HttpStatus.BAD_REQUEST, "Koordinatsystem skall vara=R (RT90 2,5 gon v)"),
    CODE_77(77, HttpStatus.BAD_REQUEST, "Koordinattyp skall vara=M (manuellt inlagd)"),
    CODE_78(78, HttpStatus.INTERNAL_SERVER_ERROR, "Uppdateringen kunde inte genomföras"),
    CODE_79(79, HttpStatus.INTERNAL_SERVER_ERROR, "Flera gator med samma namn finns inom angiven postort och postnummer"),
    CODE_80(80, HttpStatus.INTERNAL_SERVER_ERROR, "Lghnr-alias kan ej registreras"),
    CODE_81(81, HttpStatus.FORBIDDEN, "Adress är ej på lägsta nivån"),
    CODE_82(82, HttpStatus.BAD_REQUEST, "Nytt Lghnr kan bara anges i kombination med befintligt Lghnr."),
    CODE_83(83, HttpStatus.INTERNAL_SERVER_ERROR, "Ogiltigt XML-dokument."),
    CODE_84(84, HttpStatus.NOT_FOUND, "Huto saknas"),
    CODE_85(85, HttpStatus.INTERNAL_SERVER_ERROR, "Anropet till webservices har misslyckats"),
    CODE_86(86, HttpStatus.BAD_REQUEST, "Både Objektnamn och Objekttyp måste anges"),
    CODE_87(87, HttpStatus.BAD_REQUEST, "Ogiltig objekttyp"),
    CODE_88(88, HttpStatus.BAD_REQUEST, "Det får inte förekomma dubbletter av Objektnamn av samma Objkettyp"),
    CODE_89(89, HttpStatus.NOT_FOUND, "Prisnivå för svartfiber saknas"),
    CODE_90(90, HttpStatus.INTERNAL_SERVER_ERROR, "TAD-normerat gatunamn får inte väljas som en dubblettsynonym"),
    CODE_91(91, HttpStatus.CONFLICT, "Dubblettsynonym är redan knuten till adressen"),
    CODE_92(92, HttpStatus.INTERNAL_SERVER_ERROR, "Det sökta punkt-id refererar till annan information"),
    CODE_93(93, HttpStatus.INTERNAL_SERVER_ERROR, "Det sökta punkt-id refererar till en borttagen adress"),
    CODE_94(94, HttpStatus.BAD_REQUEST, "Transaktion-id måste anges"),
    CODE_95(95, HttpStatus.PAYLOAD_TOO_LARGE, "Svaret innehåller mer än 300 träffar"),
    CODE_96(96, HttpStatus.INTERNAL_SERVER_ERROR, "TAD-N flaggan kunde inte uppdateras"),
    CODE_97(97, HttpStatus.NOT_FOUND, "Det finns inga dubblettsynonymer till det givna gatunamnet i AM"),
    CODE_98(98, HttpStatus.NOT_FOUND, "Det givna Transid finns inte i AM"),
    CODE_99(99, HttpStatus.BAD_REQUEST, "Funktion måste anges"),
    CODE_100(100, HttpStatus.BAD_REQUEST, "Kalla_id måste anges"),
    CODE_101(101, HttpStatus.NOT_FOUND, "Detaljadress till den givna adressen saknas i AM"),
    CODE_102(102, HttpStatus.BAD_REQUEST, "Den givna adressen är inte en gårdsadress"),
    CODE_103(103, HttpStatus.BAD_REQUEST, "Adressen ingår inte i ett FTTH område."),
    CODE_104(104, HttpStatus.NOT_FOUND, "Det givna FTTH området saknas i AM."),
    CODE_105(105, HttpStatus.INTERNAL_SERVER_ERROR, "UTO Uppdateringen kunde inte genomföras i AM"),
    CODE_106(106, HttpStatus.NOT_FOUND, "Fastighet saknas till den givna x och y koordinaten"),
    CODE_107(107, HttpStatus.NOT_FOUND, "Postaladress saknas till den givna x och koordinaten"),
    CODE_108(108, HttpStatus.NOT_FOUND, "Region till adress saknas"),
    CODE_109(109, HttpStatus.NOT_FOUND, "Fastighetstyp till adress saknas"),
    CODE_110(110, HttpStatus.NOT_FOUND, "Region och Fastighetstyp till adress saknas"),
    CODE_111(111, HttpStatus.BAD_REQUEST, "Fas måste vara numeriskt"),
    CODE_112(112, HttpStatus.BAD_REQUEST, "FTTH område måste anges"),
    CODE_113(113, HttpStatus.CONFLICT, "Lägenhetsnummer är redan registrerat på en annan trappa."),
    CODE_114(114, HttpStatus.BAD_REQUEST, "Det finns ingångar under detta gatnr, välj rätt ingång!"),
    CODE_115(115, HttpStatus.BAD_REQUEST, "Lghnr är redan registrerat direkt under gatnr, kontrollera värde på Ingång!"),
    CODE_116(116, HttpStatus.CONFLICT, "Lghnr existerar redan under en ingång!"),
    CODE_117(117, HttpStatus.FORBIDDEN, "Det är inte tillåtet att registrera adressen på den nivå"),
    CODE_118(118, HttpStatus.INTERNAL_SERVER_ERROR, "Det givna FTTH området kan inte tas bort"),
    CODE_119(119, HttpStatus.NOT_FOUND, "Kommun till det givna FTTH området kan inte hittas i AM"),
    CODE_120(120, HttpStatus.NOT_FOUND, "Futo saknas"),
    CODE_122(122, HttpStatus.FORBIDDEN, "Lghnr från LMV/skatteverket får inte uppdateras!"),
    CODE_124(124, HttpStatus.NOT_FOUND, "Kandidatadress till det givna punkt-id saknas i AM"),
    CODE_125(125, HttpStatus.FORBIDDEN, "Adressen finns i AM, men den är ej en kandidatadress"),
    CODE_800(800, HttpStatus.INTERNAL_SERVER_ERROR, "Nätfråga kunde inte uppdateras"),
    CODE_998(998, HttpStatus.INTERNAL_SERVER_ERROR, "Ett tekniskt fel har inträffat");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    ResponseCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public static ResponseCode getResponseCode(String code) {
        int c = getResponseCodeInt(code);
        return Arrays.stream(ResponseCode.values()).filter(rc -> rc.code == c).findFirst().orElseGet(() -> {
            LoggerFactory.getLogger(ResponseCode.class).warn("No response code found for {}, defaulting to {}", code, ResponseCode.CODE_998);
            return ResponseCode.CODE_998;
        });
    }

    private static int getResponseCodeInt(String code) {
        try {
            return Integer.parseInt(code);
        } catch(NumberFormatException e) {
            return 998;
        }
    }
}
