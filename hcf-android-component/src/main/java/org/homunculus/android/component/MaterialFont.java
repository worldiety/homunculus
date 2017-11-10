/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.homunculus.android.component;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * The material icons accessible by a font, which is more or less compatible and scaleable accross multiple platforms.
 * Smaller and better than pngs and fiddling with the hdpi/mdpi/... stuff
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class MaterialFont {
    private static Typeface sMaterialIcons;

    private MaterialFont() {

    }


    public static void init(Context ctx) {
        sMaterialIcons = Typeface.createFromAsset(ctx.getAssets(), "fonts/MaterialIcons-Regular.ttf");
    }

    /**
     * Returns the typeface which contains the material icons, represented by {@link Icon}. You have to
     * call {@link #init(Context)} before accessing, otherwise the returned typeface is null
     *
     * @return the material typeface
     */
    public static Typeface getTypeface() {
        return sMaterialIcons;
    }

    public enum Icon {
        I_3D_ROTATION(0xe84d),
        I_AC_UNIT(0xeb3b),
        I_ACCESS_ALARM(0xe190),
        I_ACCESS_ALARMS(0xe191),
        I_ACCESS_TIME(0xe192),
        I_ACCESSIBILITY(0xe84e),
        I_ACCESSIBLE(0xe914),
        I_ACCOUNT_BALANCE(0xe84f),
        I_ACCOUNT_BALANCE_WALLET(0xe850),
        I_ACCOUNT_BOX(0xe851),
        I_ACCOUNT_CIRCLE(0xe853),
        I_ADB(0xe60e),
        I_ADD(0xe145),
        I_ADD_A_PHOTO(0xe439),
        I_ADD_ALARM(0xe193),
        I_ADD_ALERT(0xe003),
        I_ADD_BOX(0xe146),
        I_ADD_CIRCLE(0xe147),
        I_ADD_CIRCLE_OUTLINE(0xe148),
        I_ADD_LOCATION(0xe567),
        I_ADD_SHOPPING_CART(0xe854),
        I_ADD_TO_PHOTOS(0xe39d),
        I_ADD_TO_QUEUE(0xe05c),
        I_ADJUST(0xe39e),
        I_AIRLINE_SEAT_FLAT(0xe630),
        I_AIRLINE_SEAT_FLAT_ANGLED(0xe631),
        I_AIRLINE_SEAT_INDIVIDUAL_SUITE(0xe632),
        I_AIRLINE_SEAT_LEGROOM_EXTRA(0xe633),
        I_AIRLINE_SEAT_LEGROOM_NORMAL(0xe634),
        I_AIRLINE_SEAT_LEGROOM_REDUCED(0xe635),
        I_AIRLINE_SEAT_RECLINE_EXTRA(0xe636),
        I_AIRLINE_SEAT_RECLINE_NORMAL(0xe637),
        I_AIRPLANEMODE_ACTIVE(0xe195),
        I_AIRPLANEMODE_INACTIVE(0xe194),
        I_AIRPLAY(0xe055),
        I_AIRPORT_SHUTTLE(0xeb3c),
        I_ALARM(0xe855),
        I_ALARM_ADD(0xe856),
        I_ALARM_OFF(0xe857),
        I_ALARM_ON(0xe858),
        I_ALBUM(0xe019),
        I_ALL_INCLUSIVE(0xeb3d),
        I_ALL_OUT(0xe90b),
        I_ANDROID(0xe859),
        I_ANNOUNCEMENT(0xe85a),
        I_APPS(0xe5c3),
        I_ARCHIVE(0xe149),
        I_ARROW_BACK(0xe5c4),
        I_ARROW_DOWNWARD(0xe5db),
        I_ARROW_DROP_DOWN(0xe5c5),
        I_ARROW_DROP_DOWN_CIRCLE(0xe5c6),
        I_ARROW_DROP_UP(0xe5c7),
        I_ARROW_FORWARD(0xe5c8),
        I_ARROW_UPWARD(0xe5d8),
        I_ART_TRACK(0xe060),
        I_ASPECT_RATIO(0xe85b),
        I_ASSESSMENT(0xe85c),
        I_ASSIGNMENT(0xe85d),
        I_ASSIGNMENT_IND(0xe85e),
        I_ASSIGNMENT_LATE(0xe85f),
        I_ASSIGNMENT_RETURN(0xe860),
        I_ASSIGNMENT_RETURNED(0xe861),
        I_ASSIGNMENT_TURNED_IN(0xe862),
        I_ASSISTANT(0xe39f),
        I_ASSISTANT_PHOTO(0xe3a0),
        I_ATTACH_FILE(0xe226),
        I_ATTACH_MONEY(0xe227),
        I_ATTACHMENT(0xe2bc),
        I_AUDIOTRACK(0xe3a1),
        I_AUTORENEW(0xe863),
        I_AV_TIMER(0xe01b),
        I_BACKSPACE(0xe14a),
        I_BACKUP(0xe864),
        I_BATTERY_ALERT(0xe19c),
        I_BATTERY_CHARGING_FULL(0xe1a3),
        I_BATTERY_FULL(0xe1a4),
        I_BATTERY_STD(0xe1a5),
        I_BATTERY_UNKNOWN(0xe1a6),
        I_BEACH_ACCESS(0xeb3e),
        I_BEENHERE(0xe52d),
        I_BLOCK(0xe14b),
        I_BLUETOOTH(0xe1a7),
        I_BLUETOOTH_AUDIO(0xe60f),
        I_BLUETOOTH_CONNECTED(0xe1a8),
        I_BLUETOOTH_DISABLED(0xe1a9),
        I_BLUETOOTH_SEARCHING(0xe1aa),
        I_BLUR_CIRCULAR(0xe3a2),
        I_BLUR_LINEAR(0xe3a3),
        I_BLUR_OFF(0xe3a4),
        I_BLUR_ON(0xe3a5),
        I_BOOK(0xe865),
        I_BOOKMARK(0xe866),
        I_BOOKMARK_BORDER(0xe867),
        I_BORDER_ALL(0xe228),
        I_BORDER_BOTTOM(0xe229),
        I_BORDER_CLEAR(0xe22a),
        I_BORDER_COLOR(0xe22b),
        I_BORDER_HORIZONTAL(0xe22c),
        I_BORDER_INNER(0xe22d),
        I_BORDER_LEFT(0xe22e),
        I_BORDER_OUTER(0xe22f),
        I_BORDER_RIGHT(0xe230),
        I_BORDER_STYLE(0xe231),
        I_BORDER_TOP(0xe232),
        I_BORDER_VERTICAL(0xe233),
        I_BRANDING_WATERMARK(0xe06b),
        I_BRIGHTNESS_1(0xe3a6),
        I_BRIGHTNESS_2(0xe3a7),
        I_BRIGHTNESS_3(0xe3a8),
        I_BRIGHTNESS_4(0xe3a9),
        I_BRIGHTNESS_5(0xe3aa),
        I_BRIGHTNESS_6(0xe3ab),
        I_BRIGHTNESS_7(0xe3ac),
        I_BRIGHTNESS_AUTO(0xe1ab),
        I_BRIGHTNESS_HIGH(0xe1ac),
        I_BRIGHTNESS_LOW(0xe1ad),
        I_BRIGHTNESS_MEDIUM(0xe1ae),
        I_BROKEN_IMAGE(0xe3ad),
        I_BRUSH(0xe3ae),
        I_BUBBLE_CHART(0xe6dd),
        I_BUG_REPORT(0xe868),
        I_BUILD(0xe869),
        I_BURST_MODE(0xe43c),
        I_BUSINESS(0xe0af),
        I_BUSINESS_CENTER(0xeb3f),
        I_CACHED(0xe86a),
        I_CAKE(0xe7e9),
        I_CALL(0xe0b0),
        I_CALL_END(0xe0b1),
        I_CALL_MADE(0xe0b2),
        I_CALL_MERGE(0xe0b3),
        I_CALL_MISSED(0xe0b4),
        I_CALL_MISSED_OUTGOING(0xe0e4),
        I_CALL_RECEIVED(0xe0b5),
        I_CALL_SPLIT(0xe0b6),
        I_CALL_TO_ACTION(0xe06c),
        I_CAMERA(0xe3af),
        I_CAMERA_ALT(0xe3b0),
        I_CAMERA_ENHANCE(0xe8fc),
        I_CAMERA_FRONT(0xe3b1),
        I_CAMERA_REAR(0xe3b2),
        I_CAMERA_ROLL(0xe3b3),
        I_CANCEL(0xe5c9),
        I_CARD_GIFTCARD(0xe8f6),
        I_CARD_MEMBERSHIP(0xe8f7),
        I_CARD_TRAVEL(0xe8f8),
        I_CASINO(0xeb40),
        I_CAST(0xe307),
        I_CAST_CONNECTED(0xe308),
        I_CENTER_FOCUS_STRONG(0xe3b4),
        I_CENTER_FOCUS_WEAK(0xe3b5),
        I_CHANGE_HISTORY(0xe86b),
        I_CHAT(0xe0b7),
        I_CHAT_BUBBLE(0xe0ca),
        I_CHAT_BUBBLE_OUTLINE(0xe0cb),
        I_CHECK(0xe5ca),
        I_CHECK_BOX(0xe834),
        I_CHECK_BOX_OUTLINE_BLANK(0xe835),
        I_CHECK_CIRCLE(0xe86c),
        I_CHEVRON_LEFT(0xe5cb),
        I_CHEVRON_RIGHT(0xe5cc),
        I_CHILD_CARE(0xeb41),
        I_CHILD_FRIENDLY(0xeb42),
        I_CHROME_READER_MODE(0xe86d),
        I_CLASS(0xe86e),
        I_CLEAR(0xe14c),
        I_CLEAR_ALL(0xe0b8),
        I_CLOSE(0xe5cd),
        I_CLOSED_CAPTION(0xe01c),
        I_CLOUD(0xe2bd),
        I_CLOUD_CIRCLE(0xe2be),
        I_CLOUD_DONE(0xe2bf),
        I_CLOUD_DOWNLOAD(0xe2c0),
        I_CLOUD_OFF(0xe2c1),
        I_CLOUD_QUEUE(0xe2c2),
        I_CLOUD_UPLOAD(0xe2c3),
        I_CODE(0xe86f),
        I_COLLECTIONS(0xe3b6),
        I_COLLECTIONS_BOOKMARK(0xe431),
        I_COLOR_LENS(0xe3b7),
        I_COLORIZE(0xe3b8),
        I_COMMENT(0xe0b9),
        I_COMPARE(0xe3b9),
        I_COMPARE_ARROWS(0xe915),
        I_COMPUTER(0xe30a),
        I_CONFIRMATION_NUMBER(0xe638),
        I_CONTACT_MAIL(0xe0d0),
        I_CONTACT_PHONE(0xe0cf),
        I_CONTACTS(0xe0ba),
        I_CONTENT_COPY(0xe14d),
        I_CONTENT_CUT(0xe14e),
        I_CONTENT_PASTE(0xe14f),
        I_CONTROL_POINT(0xe3ba),
        I_CONTROL_POINT_DUPLICATE(0xe3bb),
        I_COPYRIGHT(0xe90c),
        I_CREATE(0xe150),
        I_CREATE_NEW_FOLDER(0xe2cc),
        I_CREDIT_CARD(0xe870),
        I_CROP(0xe3be),
        I_CROP_16_9(0xe3bc),
        I_CROP_3_2(0xe3bd),
        I_CROP_5_4(0xe3bf),
        I_CROP_7_5(0xe3c0),
        I_CROP_DIN(0xe3c1),
        I_CROP_FREE(0xe3c2),
        I_CROP_LANDSCAPE(0xe3c3),
        I_CROP_ORIGINAL(0xe3c4),
        I_CROP_PORTRAIT(0xe3c5),
        I_CROP_ROTATE(0xe437),
        I_CROP_SQUARE(0xe3c6),
        I_DASHBOARD(0xe871),
        I_DATA_USAGE(0xe1af),
        I_DATE_RANGE(0xe916),
        I_DEHAZE(0xe3c7),
        I_DELETE(0xe872),
        I_DELETE_FOREVER(0xe92b),
        I_DELETE_SWEEP(0xe16c),
        I_DESCRIPTION(0xe873),
        I_DESKTOP_MAC(0xe30b),
        I_DESKTOP_WINDOWS(0xe30c),
        I_DETAILS(0xe3c8),
        I_DEVELOPER_BOARD(0xe30d),
        I_DEVELOPER_MODE(0xe1b0),
        I_DEVICE_HUB(0xe335),
        I_DEVICES(0xe1b1),
        I_DEVICES_OTHER(0xe337),
        I_DIALER_SIP(0xe0bb),
        I_DIALPAD(0xe0bc),
        I_DIRECTIONS(0xe52e),
        I_DIRECTIONS_BIKE(0xe52f),
        I_DIRECTIONS_BOAT(0xe532),
        I_DIRECTIONS_BUS(0xe530),
        I_DIRECTIONS_CAR(0xe531),
        I_DIRECTIONS_RAILWAY(0xe534),
        I_DIRECTIONS_RUN(0xe566),
        I_DIRECTIONS_SUBWAY(0xe533),
        I_DIRECTIONS_TRANSIT(0xe535),
        I_DIRECTIONS_WALK(0xe536),
        I_DISC_FULL(0xe610),
        I_DNS(0xe875),
        I_DO_NOT_DISTURB(0xe612),
        I_DO_NOT_DISTURB_ALT(0xe611),
        I_DO_NOT_DISTURB_OFF(0xe643),
        I_DO_NOT_DISTURB_ON(0xe644),
        I_DOCK(0xe30e),
        I_DOMAIN(0xe7ee),
        I_DONE(0xe876),
        I_DONE_ALL(0xe877),
        I_DONUT_LARGE(0xe917),
        I_DONUT_SMALL(0xe918),
        I_DRAFTS(0xe151),
        I_DRAG_HANDLE(0xe25d),
        I_DRIVE_ETA(0xe613),
        I_DVR(0xe1b2),
        I_EDIT(0xe3c9),
        I_EDIT_LOCATION(0xe568),
        I_EJECT(0xe8fb),
        I_EMAIL(0xe0be),
        I_ENHANCED_ENCRYPTION(0xe63f),
        I_EQUALIZER(0xe01d),
        I_ERROR(0xe000),
        I_ERROR_OUTLINE(0xe001),
        I_EURO_SYMBOL(0xe926),
        I_EV_STATION(0xe56d),
        I_EVENT(0xe878),
        I_EVENT_AVAILABLE(0xe614),
        I_EVENT_BUSY(0xe615),
        I_EVENT_NOTE(0xe616),
        I_EVENT_SEAT(0xe903),
        I_EXIT_TO_APP(0xe879),
        I_EXPAND_LESS(0xe5ce),
        I_EXPAND_MORE(0xe5cf),
        I_EXPLICIT(0xe01e),
        I_EXPLORE(0xe87a),
        I_EXPOSURE(0xe3ca),
        I_EXPOSURE_NEG_1(0xe3cb),
        I_EXPOSURE_NEG_2(0xe3cc),
        I_EXPOSURE_PLUS_1(0xe3cd),
        I_EXPOSURE_PLUS_2(0xe3ce),
        I_EXPOSURE_ZERO(0xe3cf),
        I_EXTENSION(0xe87b),
        I_FACE(0xe87c),
        I_FAST_FORWARD(0xe01f),
        I_FAST_REWIND(0xe020),
        I_FAVORITE(0xe87d),
        I_FAVORITE_BORDER(0xe87e),
        I_FEATURED_PLAY_LIST(0xe06d),
        I_FEATURED_VIDEO(0xe06e),
        I_FEEDBACK(0xe87f),
        I_FIBER_DVR(0xe05d),
        I_FIBER_MANUAL_RECORD(0xe061),
        I_FIBER_NEW(0xe05e),
        I_FIBER_PIN(0xe06a),
        I_FIBER_SMART_RECORD(0xe062),
        I_FILE_DOWNLOAD(0xe2c4),
        I_FILE_UPLOAD(0xe2c6),
        I_FILTER(0xe3d3),
        I_FILTER_1(0xe3d0),
        I_FILTER_2(0xe3d1),
        I_FILTER_3(0xe3d2),
        I_FILTER_4(0xe3d4),
        I_FILTER_5(0xe3d5),
        I_FILTER_6(0xe3d6),
        I_FILTER_7(0xe3d7),
        I_FILTER_8(0xe3d8),
        I_FILTER_9(0xe3d9),
        I_FILTER_9_PLUS(0xe3da),
        I_FILTER_B_AND_W(0xe3db),
        I_FILTER_CENTER_FOCUS(0xe3dc),
        I_FILTER_DRAMA(0xe3dd),
        I_FILTER_FRAMES(0xe3de),
        I_FILTER_HDR(0xe3df),
        I_FILTER_LIST(0xe152),
        I_FILTER_NONE(0xe3e0),
        I_FILTER_TILT_SHIFT(0xe3e2),
        I_FILTER_VINTAGE(0xe3e3),
        I_FIND_IN_PAGE(0xe880),
        I_FIND_REPLACE(0xe881),
        I_FINGERPRINT(0xe90d),
        I_FIRST_PAGE(0xe5dc),
        I_FITNESS_CENTER(0xeb43),
        I_FLAG(0xe153),
        I_FLARE(0xe3e4),
        I_FLASH_AUTO(0xe3e5),
        I_FLASH_OFF(0xe3e6),
        I_FLASH_ON(0xe3e7),
        I_FLIGHT(0xe539),
        I_FLIGHT_LAND(0xe904),
        I_FLIGHT_TAKEOFF(0xe905),
        I_FLIP(0xe3e8),
        I_FLIP_TO_BACK(0xe882),
        I_FLIP_TO_FRONT(0xe883),
        I_FOLDER(0xe2c7),
        I_FOLDER_OPEN(0xe2c8),
        I_FOLDER_SHARED(0xe2c9),
        I_FOLDER_SPECIAL(0xe617),
        I_FONT_DOWNLOAD(0xe167),
        I_FORMAT_ALIGN_CENTER(0xe234),
        I_FORMAT_ALIGN_JUSTIFY(0xe235),
        I_FORMAT_ALIGN_LEFT(0xe236),
        I_FORMAT_ALIGN_RIGHT(0xe237),
        I_FORMAT_BOLD(0xe238),
        I_FORMAT_CLEAR(0xe239),
        I_FORMAT_COLOR_FILL(0xe23a),
        I_FORMAT_COLOR_RESET(0xe23b),
        I_FORMAT_COLOR_TEXT(0xe23c),
        I_FORMAT_INDENT_DECREASE(0xe23d),
        I_FORMAT_INDENT_INCREASE(0xe23e),
        I_FORMAT_ITALIC(0xe23f),
        I_FORMAT_LINE_SPACING(0xe240),
        I_FORMAT_LIST_BULLETED(0xe241),
        I_FORMAT_LIST_NUMBERED(0xe242),
        I_FORMAT_PAINT(0xe243),
        I_FORMAT_QUOTE(0xe244),
        I_FORMAT_SHAPES(0xe25e),
        I_FORMAT_SIZE(0xe245),
        I_FORMAT_STRIKETHROUGH(0xe246),
        I_FORMAT_TEXTDIRECTION_L_TO_R(0xe247),
        I_FORMAT_TEXTDIRECTION_R_TO_L(0xe248),
        I_FORMAT_UNDERLINED(0xe249),
        I_FORUM(0xe0bf),
        I_FORWARD(0xe154),
        I_FORWARD_10(0xe056),
        I_FORWARD_30(0xe057),
        I_FORWARD_5(0xe058),
        I_FREE_BREAKFAST(0xeb44),
        I_FULLSCREEN(0xe5d0),
        I_FULLSCREEN_EXIT(0xe5d1),
        I_FUNCTIONS(0xe24a),
        I_G_TRANSLATE(0xe927),
        I_GAMEPAD(0xe30f),
        I_GAMES(0xe021),
        I_GAVEL(0xe90e),
        I_GESTURE(0xe155),
        I_GET_APP(0xe884),
        I_GIF(0xe908),
        I_GOLF_COURSE(0xeb45),
        I_GPS_FIXED(0xe1b3),
        I_GPS_NOT_FIXED(0xe1b4),
        I_GPS_OFF(0xe1b5),
        I_GRADE(0xe885),
        I_GRADIENT(0xe3e9),
        I_GRAIN(0xe3ea),
        I_GRAPHIC_EQ(0xe1b8),
        I_GRID_OFF(0xe3eb),
        I_GRID_ON(0xe3ec),
        I_GROUP(0xe7ef),
        I_GROUP_ADD(0xe7f0),
        I_GROUP_WORK(0xe886),
        I_HD(0xe052),
        I_HDR_OFF(0xe3ed),
        I_HDR_ON(0xe3ee),
        I_HDR_STRONG(0xe3f1),
        I_HDR_WEAK(0xe3f2),
        I_HEADSET(0xe310),
        I_HEADSET_MIC(0xe311),
        I_HEALING(0xe3f3),
        I_HEARING(0xe023),
        I_HELP(0xe887),
        I_HELP_OUTLINE(0xe8fd),
        I_HIGH_QUALITY(0xe024),
        I_HIGHLIGHT(0xe25f),
        I_HIGHLIGHT_OFF(0xe888),
        I_HISTORY(0xe889),
        I_HOME(0xe88a),
        I_HOT_TUB(0xeb46),
        I_HOTEL(0xe53a),
        I_HOURGLASS_EMPTY(0xe88b),
        I_HOURGLASS_FULL(0xe88c),
        I_HTTP(0xe902),
        I_HTTPS(0xe88d),
        I_IMAGE(0xe3f4),
        I_IMAGE_ASPECT_RATIO(0xe3f5),
        I_IMPORT_CONTACTS(0xe0e0),
        I_IMPORT_EXPORT(0xe0c3),
        I_IMPORTANT_DEVICES(0xe912),
        I_INBOX(0xe156),
        I_INDETERMINATE_CHECK_BOX(0xe909),
        I_INFO(0xe88e),
        I_INFO_OUTLINE(0xe88f),
        I_INPUT(0xe890),
        I_INSERT_CHART(0xe24b),
        I_INSERT_COMMENT(0xe24c),
        I_INSERT_DRIVE_FILE(0xe24d),
        I_INSERT_EMOTICON(0xe24e),
        I_INSERT_INVITATION(0xe24f),
        I_INSERT_LINK(0xe250),
        I_INSERT_PHOTO(0xe251),
        I_INVERT_COLORS(0xe891),
        I_INVERT_COLORS_OFF(0xe0c4),
        I_ISO(0xe3f6),
        I_KEYBOARD(0xe312),
        I_KEYBOARD_ARROW_DOWN(0xe313),
        I_KEYBOARD_ARROW_LEFT(0xe314),
        I_KEYBOARD_ARROW_RIGHT(0xe315),
        I_KEYBOARD_ARROW_UP(0xe316),
        I_KEYBOARD_BACKSPACE(0xe317),
        I_KEYBOARD_CAPSLOCK(0xe318),
        I_KEYBOARD_HIDE(0xe31a),
        I_KEYBOARD_RETURN(0xe31b),
        I_KEYBOARD_TAB(0xe31c),
        I_KEYBOARD_VOICE(0xe31d),
        I_KITCHEN(0xeb47),
        I_LABEL(0xe892),
        I_LABEL_OUTLINE(0xe893),
        I_LANDSCAPE(0xe3f7),
        I_LANGUAGE(0xe894),
        I_LAPTOP(0xe31e),
        I_LAPTOP_CHROMEBOOK(0xe31f),
        I_LAPTOP_MAC(0xe320),
        I_LAPTOP_WINDOWS(0xe321),
        I_LAST_PAGE(0xe5dd),
        I_LAUNCH(0xe895),
        I_LAYERS(0xe53b),
        I_LAYERS_CLEAR(0xe53c),
        I_LEAK_ADD(0xe3f8),
        I_LEAK_REMOVE(0xe3f9),
        I_LENS(0xe3fa),
        I_LIBRARY_ADD(0xe02e),
        I_LIBRARY_BOOKS(0xe02f),
        I_LIBRARY_MUSIC(0xe030),
        I_LIGHTBULB_OUTLINE(0xe90f),
        I_LINE_STYLE(0xe919),
        I_LINE_WEIGHT(0xe91a),
        I_LINEAR_SCALE(0xe260),
        I_LINK(0xe157),
        I_LINKED_CAMERA(0xe438),
        I_LIST(0xe896),
        I_LIVE_HELP(0xe0c6),
        I_LIVE_TV(0xe639),
        I_LOCAL_ACTIVITY(0xe53f),
        I_LOCAL_AIRPORT(0xe53d),
        I_LOCAL_ATM(0xe53e),
        I_LOCAL_BAR(0xe540),
        I_LOCAL_CAFE(0xe541),
        I_LOCAL_CAR_WASH(0xe542),
        I_LOCAL_CONVENIENCE_STORE(0xe543),
        I_LOCAL_DINING(0xe556),
        I_LOCAL_DRINK(0xe544),
        I_LOCAL_FLORIST(0xe545),
        I_LOCAL_GAS_STATION(0xe546),
        I_LOCAL_GROCERY_STORE(0xe547),
        I_LOCAL_HOSPITAL(0xe548),
        I_LOCAL_HOTEL(0xe549),
        I_LOCAL_LAUNDRY_SERVICE(0xe54a),
        I_LOCAL_LIBRARY(0xe54b),
        I_LOCAL_MALL(0xe54c),
        I_LOCAL_MOVIES(0xe54d),
        I_LOCAL_OFFER(0xe54e),
        I_LOCAL_PARKING(0xe54f),
        I_LOCAL_PHARMACY(0xe550),
        I_LOCAL_PHONE(0xe551),
        I_LOCAL_PIZZA(0xe552),
        I_LOCAL_PLAY(0xe553),
        I_LOCAL_POST_OFFICE(0xe554),
        I_LOCAL_PRINTSHOP(0xe555),
        I_LOCAL_SEE(0xe557),
        I_LOCAL_SHIPPING(0xe558),
        I_LOCAL_TAXI(0xe559),
        I_LOCATION_CITY(0xe7f1),
        I_LOCATION_DISABLED(0xe1b6),
        I_LOCATION_OFF(0xe0c7),
        I_LOCATION_ON(0xe0c8),
        I_LOCATION_SEARCHING(0xe1b7),
        I_LOCK(0xe897),
        I_LOCK_OPEN(0xe898),
        I_LOCK_OUTLINE(0xe899),
        I_LOOKS(0xe3fc),
        I_LOOKS_3(0xe3fb),
        I_LOOKS_4(0xe3fd),
        I_LOOKS_5(0xe3fe),
        I_LOOKS_6(0xe3ff),
        I_LOOKS_ONE(0xe400),
        I_LOOKS_TWO(0xe401),
        I_LOOP(0xe028),
        I_LOUPE(0xe402),
        I_LOW_PRIORITY(0xe16d),
        I_LOYALTY(0xe89a),
        I_MAIL(0xe158),
        I_MAIL_OUTLINE(0xe0e1),
        I_MAP(0xe55b),
        I_MARKUNREAD(0xe159),
        I_MARKUNREAD_MAILBOX(0xe89b),
        I_MEMORY(0xe322),
        I_MENU(0xe5d2),
        I_MERGE_TYPE(0xe252),
        I_MESSAGE(0xe0c9),
        I_MIC(0xe029),
        I_MIC_NONE(0xe02a),
        I_MIC_OFF(0xe02b),
        I_MMS(0xe618),
        I_MODE_COMMENT(0xe253),
        I_MODE_EDIT(0xe254),
        I_MONETIZATION_ON(0xe263),
        I_MONEY_OFF(0xe25c),
        I_MONOCHROME_PHOTOS(0xe403),
        I_MOOD(0xe7f2),
        I_MOOD_BAD(0xe7f3),
        I_MORE(0xe619),
        I_MORE_HORIZ(0xe5d3),
        I_MORE_VERT(0xe5d4),
        I_MOTORCYCLE(0xe91b),
        I_MOUSE(0xe323),
        I_MOVE_TO_INBOX(0xe168),
        I_MOVIE(0xe02c),
        I_MOVIE_CREATION(0xe404),
        I_MOVIE_FILTER(0xe43a),
        I_MULTILINE_CHART(0xe6df),
        I_MUSIC_NOTE(0xe405),
        I_MUSIC_VIDEO(0xe063),
        I_MY_LOCATION(0xe55c),
        I_NATURE(0xe406),
        I_NATURE_PEOPLE(0xe407),
        I_NAVIGATE_BEFORE(0xe408),
        I_NAVIGATE_NEXT(0xe409),
        I_NAVIGATION(0xe55d),
        I_NEAR_ME(0xe569),
        I_NETWORK_CELL(0xe1b9),
        I_NETWORK_CHECK(0xe640),
        I_NETWORK_LOCKED(0xe61a),
        I_NETWORK_WIFI(0xe1ba),
        I_NEW_RELEASES(0xe031),
        I_NEXT_WEEK(0xe16a),
        I_NFC(0xe1bb),
        I_NO_ENCRYPTION(0xe641),
        I_NO_SIM(0xe0cc),
        I_NOT_INTERESTED(0xe033),
        I_NOTE(0xe06f),
        I_NOTE_ADD(0xe89c),
        I_NOTIFICATIONS(0xe7f4),
        I_NOTIFICATIONS_ACTIVE(0xe7f7),
        I_NOTIFICATIONS_NONE(0xe7f5),
        I_NOTIFICATIONS_OFF(0xe7f6),
        I_NOTIFICATIONS_PAUSED(0xe7f8),
        I_OFFLINE_PIN(0xe90a),
        I_ONDEMAND_VIDEO(0xe63a),
        I_OPACITY(0xe91c),
        I_OPEN_IN_BROWSER(0xe89d),
        I_OPEN_IN_NEW(0xe89e),
        I_OPEN_WITH(0xe89f),
        I_PAGES(0xe7f9),
        I_PAGEVIEW(0xe8a0),
        I_PALETTE(0xe40a),
        I_PAN_TOOL(0xe925),
        I_PANORAMA(0xe40b),
        I_PANORAMA_FISH_EYE(0xe40c),
        I_PANORAMA_HORIZONTAL(0xe40d),
        I_PANORAMA_VERTICAL(0xe40e),
        I_PANORAMA_WIDE_ANGLE(0xe40f),
        I_PARTY_MODE(0xe7fa),
        I_PAUSE(0xe034),
        I_PAUSE_CIRCLE_FILLED(0xe035),
        I_PAUSE_CIRCLE_OUTLINE(0xe036),
        I_PAYMENT(0xe8a1),
        I_PEOPLE(0xe7fb),
        I_PEOPLE_OUTLINE(0xe7fc),
        I_PERM_CAMERA_MIC(0xe8a2),
        I_PERM_CONTACT_CALENDAR(0xe8a3),
        I_PERM_DATA_SETTING(0xe8a4),
        I_PERM_DEVICE_INFORMATION(0xe8a5),
        I_PERM_IDENTITY(0xe8a6),
        I_PERM_MEDIA(0xe8a7),
        I_PERM_PHONE_MSG(0xe8a8),
        I_PERM_SCAN_WIFI(0xe8a9),
        I_PERSON(0xe7fd),
        I_PERSON_ADD(0xe7fe),
        I_PERSON_OUTLINE(0xe7ff),
        I_PERSON_PIN(0xe55a),
        I_PERSON_PIN_CIRCLE(0xe56a),
        I_PERSONAL_VIDEO(0xe63b),
        I_PETS(0xe91d),
        I_PHONE(0xe0cd),
        I_PHONE_ANDROID(0xe324),
        I_PHONE_BLUETOOTH_SPEAKER(0xe61b),
        I_PHONE_FORWARDED(0xe61c),
        I_PHONE_IN_TALK(0xe61d),
        I_PHONE_IPHONE(0xe325),
        I_PHONE_LOCKED(0xe61e),
        I_PHONE_MISSED(0xe61f),
        I_PHONE_PAUSED(0xe620),
        I_PHONELINK(0xe326),
        I_PHONELINK_ERASE(0xe0db),
        I_PHONELINK_LOCK(0xe0dc),
        I_PHONELINK_OFF(0xe327),
        I_PHONELINK_RING(0xe0dd),
        I_PHONELINK_SETUP(0xe0de),
        I_PHOTO(0xe410),
        I_PHOTO_ALBUM(0xe411),
        I_PHOTO_CAMERA(0xe412),
        I_PHOTO_FILTER(0xe43b),
        I_PHOTO_LIBRARY(0xe413),
        I_PHOTO_SIZE_SELECT_ACTUAL(0xe432),
        I_PHOTO_SIZE_SELECT_LARGE(0xe433),
        I_PHOTO_SIZE_SELECT_SMALL(0xe434),
        I_PICTURE_AS_PDF(0xe415),
        I_PICTURE_IN_PICTURE(0xe8aa),
        I_PICTURE_IN_PICTURE_ALT(0xe911),
        I_PIE_CHART(0xe6c4),
        I_PIE_CHART_OUTLINED(0xe6c5),
        I_PIN_DROP(0xe55e),
        I_PLACE(0xe55f),
        I_PLAY_ARROW(0xe037),
        I_PLAY_CIRCLE_FILLED(0xe038),
        I_PLAY_CIRCLE_OUTLINE(0xe039),
        I_PLAY_FOR_WORK(0xe906),
        I_PLAYLIST_ADD(0xe03b),
        I_PLAYLIST_ADD_CHECK(0xe065),
        I_PLAYLIST_PLAY(0xe05f),
        I_PLUS_ONE(0xe800),
        I_POLL(0xe801),
        I_POLYMER(0xe8ab),
        I_POOL(0xeb48),
        I_PORTABLE_WIFI_OFF(0xe0ce),
        I_PORTRAIT(0xe416),
        I_POWER(0xe63c),
        I_POWER_INPUT(0xe336),
        I_POWER_SETTINGS_NEW(0xe8ac),
        I_PREGNANT_WOMAN(0xe91e),
        I_PRESENT_TO_ALL(0xe0df),
        I_PRINT(0xe8ad),
        I_PRIORITY_HIGH(0xe645),
        I_PUBLIC(0xe80b),
        I_PUBLISH(0xe255),
        I_QUERY_BUILDER(0xe8ae),
        I_QUESTION_ANSWER(0xe8af),
        I_QUEUE(0xe03c),
        I_QUEUE_MUSIC(0xe03d),
        I_QUEUE_PLAY_NEXT(0xe066),
        I_RADIO(0xe03e),
        I_RADIO_BUTTON_CHECKED(0xe837),
        I_RADIO_BUTTON_UNCHECKED(0xe836),
        I_RATE_REVIEW(0xe560),
        I_RECEIPT(0xe8b0),
        I_RECENT_ACTORS(0xe03f),
        I_RECORD_VOICE_OVER(0xe91f),
        I_REDEEM(0xe8b1),
        I_REDO(0xe15a),
        I_REFRESH(0xe5d5),
        I_REMOVE(0xe15b),
        I_REMOVE_CIRCLE(0xe15c),
        I_REMOVE_CIRCLE_OUTLINE(0xe15d),
        I_REMOVE_FROM_QUEUE(0xe067),
        I_REMOVE_RED_EYE(0xe417),
        I_REMOVE_SHOPPING_CART(0xe928),
        I_REORDER(0xe8fe),
        I_REPEAT(0xe040),
        I_REPEAT_ONE(0xe041),
        I_REPLAY(0xe042),
        I_REPLAY_10(0xe059),
        I_REPLAY_30(0xe05a),
        I_REPLAY_5(0xe05b),
        I_REPLY(0xe15e),
        I_REPLY_ALL(0xe15f),
        I_REPORT(0xe160),
        I_REPORT_PROBLEM(0xe8b2),
        I_RESTAURANT(0xe56c),
        I_RESTAURANT_MENU(0xe561),
        I_RESTORE(0xe8b3),
        I_RESTORE_PAGE(0xe929),
        I_RING_VOLUME(0xe0d1),
        I_ROOM(0xe8b4),
        I_ROOM_SERVICE(0xeb49),
        I_ROTATE_90_DEGREES_CCW(0xe418),
        I_ROTATE_LEFT(0xe419),
        I_ROTATE_RIGHT(0xe41a),
        I_ROUNDED_CORNER(0xe920),
        I_ROUTER(0xe328),
        I_ROWING(0xe921),
        I_RSS_FEED(0xe0e5),
        I_RV_HOOKUP(0xe642),
        I_SATELLITE(0xe562),
        I_SAVE(0xe161),
        I_SCANNER(0xe329),
        I_SCHEDULE(0xe8b5),
        I_SCHOOL(0xe80c),
        I_SCREEN_LOCK_LANDSCAPE(0xe1be),
        I_SCREEN_LOCK_PORTRAIT(0xe1bf),
        I_SCREEN_LOCK_ROTATION(0xe1c0),
        I_SCREEN_ROTATION(0xe1c1),
        I_SCREEN_SHARE(0xe0e2),
        I_SD_CARD(0xe623),
        I_SD_STORAGE(0xe1c2),
        I_SEARCH(0xe8b6),
        I_SECURITY(0xe32a),
        I_SELECT_ALL(0xe162),
        I_SEND(0xe163),
        I_SENTIMENT_DISSATISFIED(0xe811),
        I_SENTIMENT_NEUTRAL(0xe812),
        I_SENTIMENT_SATISFIED(0xe813),
        I_SENTIMENT_VERY_DISSATISFIED(0xe814),
        I_SENTIMENT_VERY_SATISFIED(0xe815),
        I_SETTINGS(0xe8b8),
        I_SETTINGS_APPLICATIONS(0xe8b9),
        I_SETTINGS_BACKUP_RESTORE(0xe8ba),
        I_SETTINGS_BLUETOOTH(0xe8bb),
        I_SETTINGS_BRIGHTNESS(0xe8bd),
        I_SETTINGS_CELL(0xe8bc),
        I_SETTINGS_ETHERNET(0xe8be),
        I_SETTINGS_INPUT_ANTENNA(0xe8bf),
        I_SETTINGS_INPUT_COMPONENT(0xe8c0),
        I_SETTINGS_INPUT_COMPOSITE(0xe8c1),
        I_SETTINGS_INPUT_HDMI(0xe8c2),
        I_SETTINGS_INPUT_SVIDEO(0xe8c3),
        I_SETTINGS_OVERSCAN(0xe8c4),
        I_SETTINGS_PHONE(0xe8c5),
        I_SETTINGS_POWER(0xe8c6),
        I_SETTINGS_REMOTE(0xe8c7),
        I_SETTINGS_SYSTEM_DAYDREAM(0xe1c3),
        I_SETTINGS_VOICE(0xe8c8),
        I_SHARE(0xe80d),
        I_SHOP(0xe8c9),
        I_SHOP_TWO(0xe8ca),
        I_SHOPPING_BASKET(0xe8cb),
        I_SHOPPING_CART(0xe8cc),
        I_SHORT_TEXT(0xe261),
        I_SHOW_CHART(0xe6e1),
        I_SHUFFLE(0xe043),
        I_SIGNAL_CELLULAR_4_BAR(0xe1c8),
        I_SIGNAL_CELLULAR_CONNECTED_NO_INTERNET_4_BAR(0xe1cd),
        I_SIGNAL_CELLULAR_NO_SIM(0xe1ce),
        I_SIGNAL_CELLULAR_NULL(0xe1cf),
        I_SIGNAL_CELLULAR_OFF(0xe1d0),
        I_SIGNAL_WIFI_4_BAR(0xe1d8),
        I_SIGNAL_WIFI_4_BAR_LOCK(0xe1d9),
        I_SIGNAL_WIFI_OFF(0xe1da),
        I_SIM_CARD(0xe32b),
        I_SIM_CARD_ALERT(0xe624),
        I_SKIP_NEXT(0xe044),
        I_SKIP_PREVIOUS(0xe045),
        I_SLIDESHOW(0xe41b),
        I_SLOW_MOTION_VIDEO(0xe068),
        I_SMARTPHONE(0xe32c),
        I_SMOKE_FREE(0xeb4a),
        I_SMOKING_ROOMS(0xeb4b),
        I_SMS(0xe625),
        I_SMS_FAILED(0xe626),
        I_SNOOZE(0xe046),
        I_SORT(0xe164),
        I_SORT_BY_ALPHA(0xe053),
        I_SPA(0xeb4c),
        I_SPACE_BAR(0xe256),
        I_SPEAKER(0xe32d),
        I_SPEAKER_GROUP(0xe32e),
        I_SPEAKER_NOTES(0xe8cd),
        I_SPEAKER_NOTES_OFF(0xe92a),
        I_SPEAKER_PHONE(0xe0d2),
        I_SPELLCHECK(0xe8ce),
        I_STAR(0xe838),
        I_STAR_BORDER(0xe83a),
        I_STAR_HALF(0xe839),
        I_STARS(0xe8d0),
        I_STAY_CURRENT_LANDSCAPE(0xe0d3),
        I_STAY_CURRENT_PORTRAIT(0xe0d4),
        I_STAY_PRIMARY_LANDSCAPE(0xe0d5),
        I_STAY_PRIMARY_PORTRAIT(0xe0d6),
        I_STOP(0xe047),
        I_STOP_SCREEN_SHARE(0xe0e3),
        I_STORAGE(0xe1db),
        I_STORE(0xe8d1),
        I_STORE_MALL_DIRECTORY(0xe563),
        I_STRAIGHTEN(0xe41c),
        I_STREETVIEW(0xe56e),
        I_STRIKETHROUGH_S(0xe257),
        I_STYLE(0xe41d),
        I_SUBDIRECTORY_ARROW_LEFT(0xe5d9),
        I_SUBDIRECTORY_ARROW_RIGHT(0xe5da),
        I_SUBJECT(0xe8d2),
        I_SUBSCRIPTIONS(0xe064),
        I_SUBTITLES(0xe048),
        I_SUBWAY(0xe56f),
        I_SUPERVISOR_ACCOUNT(0xe8d3),
        I_SURROUND_SOUND(0xe049),
        I_SWAP_CALLS(0xe0d7),
        I_SWAP_HORIZ(0xe8d4),
        I_SWAP_VERT(0xe8d5),
        I_SWAP_VERTICAL_CIRCLE(0xe8d6),
        I_SWITCH_CAMERA(0xe41e),
        I_SWITCH_VIDEO(0xe41f),
        I_SYNC(0xe627),
        I_SYNC_DISABLED(0xe628),
        I_SYNC_PROBLEM(0xe629),
        I_SYSTEM_UPDATE(0xe62a),
        I_SYSTEM_UPDATE_ALT(0xe8d7),
        I_TAB(0xe8d8),
        I_TAB_UNSELECTED(0xe8d9),
        I_TABLET(0xe32f),
        I_TABLET_ANDROID(0xe330),
        I_TABLET_MAC(0xe331),
        I_TAG_FACES(0xe420),
        I_TAP_AND_PLAY(0xe62b),
        I_TERRAIN(0xe564),
        I_TEXT_FIELDS(0xe262),
        I_TEXT_FORMAT(0xe165),
        I_TEXTSMS(0xe0d8),
        I_TEXTURE(0xe421),
        I_THEATERS(0xe8da),
        I_THUMB_DOWN(0xe8db),
        I_THUMB_UP(0xe8dc),
        I_THUMBS_UP_DOWN(0xe8dd),
        I_TIME_TO_LEAVE(0xe62c),
        I_TIMELAPSE(0xe422),
        I_TIMELINE(0xe922),
        I_TIMER(0xe425),
        I_TIMER_10(0xe423),
        I_TIMER_3(0xe424),
        I_TIMER_OFF(0xe426),
        I_TITLE(0xe264),
        I_TOC(0xe8de),
        I_TODAY(0xe8df),
        I_TOLL(0xe8e0),
        I_TONALITY(0xe427),
        I_TOUCH_APP(0xe913),
        I_TOYS(0xe332),
        I_TRACK_CHANGES(0xe8e1),
        I_TRAFFIC(0xe565),
        I_TRAIN(0xe570),
        I_TRAM(0xe571),
        I_TRANSFER_WITHIN_A_STATION(0xe572),
        I_TRANSFORM(0xe428),
        I_TRANSLATE(0xe8e2),
        I_TRENDING_DOWN(0xe8e3),
        I_TRENDING_FLAT(0xe8e4),
        I_TRENDING_UP(0xe8e5),
        I_TUNE(0xe429),
        I_TURNED_IN(0xe8e6),
        I_TURNED_IN_NOT(0xe8e7),
        I_TV(0xe333),
        I_UNARCHIVE(0xe169),
        I_UNDO(0xe166),
        I_UNFOLD_LESS(0xe5d6),
        I_UNFOLD_MORE(0xe5d7),
        I_UPDATE(0xe923),
        I_USB(0xe1e0),
        I_VERIFIED_USER(0xe8e8),
        I_VERTICAL_ALIGN_BOTTOM(0xe258),
        I_VERTICAL_ALIGN_CENTER(0xe259),
        I_VERTICAL_ALIGN_TOP(0xe25a),
        I_VIBRATION(0xe62d),
        I_VIDEO_CALL(0xe070),
        I_VIDEO_LABEL(0xe071),
        I_VIDEO_LIBRARY(0xe04a),
        I_VIDEOCAM(0xe04b),
        I_VIDEOCAM_OFF(0xe04c),
        I_VIDEOGAME_ASSET(0xe338),
        I_VIEW_AGENDA(0xe8e9),
        I_VIEW_ARRAY(0xe8ea),
        I_VIEW_CAROUSEL(0xe8eb),
        I_VIEW_COLUMN(0xe8ec),
        I_VIEW_COMFY(0xe42a),
        I_VIEW_COMPACT(0xe42b),
        I_VIEW_DAY(0xe8ed),
        I_VIEW_HEADLINE(0xe8ee),
        I_VIEW_LIST(0xe8ef),
        I_VIEW_MODULE(0xe8f0),
        I_VIEW_QUILT(0xe8f1),
        I_VIEW_STREAM(0xe8f2),
        I_VIEW_WEEK(0xe8f3),
        I_VIGNETTE(0xe435),
        I_VISIBILITY(0xe8f4),
        I_VISIBILITY_OFF(0xe8f5),
        I_VOICE_CHAT(0xe62e),
        I_VOICEMAIL(0xe0d9),
        I_VOLUME_DOWN(0xe04d),
        I_VOLUME_MUTE(0xe04e),
        I_VOLUME_OFF(0xe04f),
        I_VOLUME_UP(0xe050),
        I_VPN_KEY(0xe0da),
        I_VPN_LOCK(0xe62f),
        I_WALLPAPER(0xe1bc),
        I_WARNING(0xe002),
        I_WATCH(0xe334),
        I_WATCH_LATER(0xe924),
        I_WB_AUTO(0xe42c),
        I_WB_CLOUDY(0xe42d),
        I_WB_INCANDESCENT(0xe42e),
        I_WB_IRIDESCENT(0xe436),
        I_WB_SUNNY(0xe430),
        I_WC(0xe63d),
        I_WEB(0xe051),
        I_WEB_ASSET(0xe069),
        I_WEEKEND(0xe16b),
        I_WHATSHOT(0xe80e),
        I_WIDGETS(0xe1bd),
        I_WIFI(0xe63e),
        I_WIFI_LOCK(0xe1e1),
        I_WIFI_TETHERING(0xe1e2),
        I_WORK(0xe8f9),
        I_WRAP_TEXT(0xe25b),
        I_YOUTUBE_SEARCHED_FOR(0xe8fa),
        I_ZOOM_IN(0xe8ff),
        I_ZOOM_OUT(0xe900),
        I_ZOOM_OUT_MAP(0xe56b),;
        private final int mCodepoint;
        private final String mText;

        Icon(int cp) {
            mCodepoint = cp;
            mText = new String(Character.toChars(cp));
        }

        /**
         * Returns this icon as a text which is insertable into a text view
         *
         * @return the codepoint as a string
         */
        public String asText() {
            return mText;
        }

        /**
         * Returns the unicode code point of this icon
         *
         * @return the codepoint
         */
        public int getCodepoint() {
            return mCodepoint;
        }
    }


}