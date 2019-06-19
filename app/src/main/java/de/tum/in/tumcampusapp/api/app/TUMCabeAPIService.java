package de.tum.in.tumcampusapp.api.app;

import java.util.HashMap;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.model.DeviceRegister;
import de.tum.in.tumcampusapp.api.app.model.DeviceUploadFcmToken;
import de.tum.in.tumcampusapp.api.app.model.ObfuscatedIdsUpload;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeStatus;
import de.tum.in.tumcampusapp.api.app.model.TumCabeVerification;
import de.tum.in.tumcampusapp.api.app.model.UploadStatus;
import de.tum.in.tumcampusapp.component.other.locations.model.BuildingToGps;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Feedback;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.FeedbackResult;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderCoordinate;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderMap;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderSchedule;
import de.tum.in.tumcampusapp.component.ui.alarm.model.FcmNotification;
import de.tum.in.tumcampusapp.component.ui.alarm.model.FcmNotificationLocation;
import de.tum.in.tumcampusapp.component.ui.barrierfree.model.BarrierfreeContact;
import de.tum.in.tumcampusapp.component.ui.barrierfree.model.BarrierfreeMoreInfo;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.Cafeteria;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMember;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatMessage;
import de.tum.in.tumcampusapp.component.ui.chat.model.ChatRoom;
import de.tum.in.tumcampusapp.component.ui.news.model.News;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsAlert;
import de.tum.in.tumcampusapp.component.ui.news.model.NewsSources;
import de.tum.in.tumcampusapp.component.ui.openinghour.model.Location;
import de.tum.in.tumcampusapp.component.ui.studyroom.model.StudyRoomGroup;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Event;
import de.tum.in.tumcampusapp.component.ui.ticket.model.Ticket;
import de.tum.in.tumcampusapp.component.ui.ticket.model.TicketType;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.EphimeralKey;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketPurchaseStripe;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservation;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketReservationResponse;
import de.tum.in.tumcampusapp.component.ui.ticket.payload.TicketStatus;
import de.tum.in.tumcampusapp.component.ui.tufilm.model.Kino;
import de.tum.in.tumcampusapp.component.ui.updatenote.model.UpdateNote;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface TumCabeApiService {

    // Group chat
    @Headers("x-requires-verification: true")
    @POST("chat/rooms/")
    Call<ChatRoom> createRoom(@Body ChatRoom data);

    @GET("chat/rooms/{room}")
    Call<ChatRoom> getChatRoom(@Path("room") int id);

    @Headers("x-requires-verification: true")
    @POST("chat/rooms/{room}/leave/")
    Call<ChatRoom> leaveChatRoom(@Path("room") int roomId);

    @Headers("x-requires-verification: true")
    @POST("chat/rooms/{room}/add/{member}")
    Call<ChatRoom> addUserToChat(
            @Path("room") int roomId,
            @Path("member") int userId
    );

    // Get/Update single message
    @Headers("x-requires-verification: true")
    @PUT("chat/rooms/{room}/message/")
    Observable<ChatMessage> sendMessage(
            @Path("room") int roomId,
            @Body ChatMessage data
    );

    @Headers("x-requires-verification: true")
    @PUT("chat/rooms/{room}/message/{message}/")
    Observable<ChatMessage> updateMessage(
            @Path("room") int roomId,
            @Path("message") int messageId,
            @Body ChatMessage data
    );

    // Get all recent messages or older ones
    @Headers("x-requires-verification: true")
    @POST("chat/rooms/{room}/messages/{page}/")
    Observable<List<ChatMessage>> getMessages(
            @Path("room") int roomId,
            @Path("page") long messageId
    );

    @Headers("x-requires-verification: true")
    @POST("chat/rooms/{room}/messages/")
    Observable<List<ChatMessage>> getNewMessages(
            @Path("room") int roomId
    );

    @POST("chat/members/")
    Call<ChatMember> createMember(@Body ChatMember chatMember);

    @GET("chat/members/{lrz_id}/")
    Call<ChatMember> getMember(@Path("lrz_id") String lrzId);

    @GET("chat/members/search/{query}/")
    Call<List<ChatMember>> searchMemberByName(@Path("query") String nameQuery);

    @Headers("x-requires-verification: true")
    @POST("chat/members/{memberId}/rooms/")
    Call<List<ChatRoom>> getMemberRooms(
            @Path("memberId") int memberId
    );

    @Headers("x-requires-verification: true")
    @POST("members/uploadIds/{lrzId}/")
    Observable<TUMCabeStatus> uploadObfuscatedIds(
            @Path("lrzId") String lrzId,
            @Body ObfuscatedIdsUpload ids
    );

    @GET("notifications/{notification}/")
    Call<FcmNotification> getNotification(@Path("notification") int notification);

    @GET("notifications/confirm/{notification}/")
    Call<String> confirm(@Path("notification") int notification);

    @GET("locations/{locationId}/")
    Call<FcmNotificationLocation> getLocation(@Path("locationId") int locationId);

    // Device
    @POST("device/register/")
    Call<TUMCabeStatus> deviceRegister(@Body DeviceRegister verification);

    @GET("device/verifyKey/")
    Call<TUMCabeStatus> verifyKey();

    @POST("device/addGcmToken/")
    Call<TUMCabeStatus> deviceUploadGcmToken(@Body DeviceUploadFcmToken verification);

    @GET("device/uploaded/{lrzId}")
    Call<UploadStatus> getUploadStatus(@Path("lrzId") String lrzId);

    // Barrier free contacts
    @GET("barrierfree/contacts/")
    Call<List<BarrierfreeContact>> getBarrierfreeContactList();

    // Barrier free More Info
    @GET("barrierfree/moreInformation/")
    Call<List<BarrierfreeMoreInfo>> getMoreInfoList();

    // Barrier free elevator list
    @GET("barrierfree/listOfElevators/")
    Call<List<RoomFinderRoom>> getListOfElevators();

    // Barrier free nearby list
    @GET("barrierfree/nerby/{buildingId}/")
    Call<List<RoomFinderRoom>> getListOfNearbyFacilities(@Path("buildingId") String buildingId);

    // building to gps information
    @GET("barrierfree/getBuilding2Gps/")
    Call<List<BuildingToGps>> getBuilding2Gps();

    // RoomFinder maps
    @GET("roomfinder/room/availableMaps/{archId}")
    Call<List<RoomFinderMap>> fetchAvailableMaps(@Path("archId") String archId);

    // RoomFinder maps
    @GET("roomfinder/room/search/{searchStrings}")
    Call<List<RoomFinderRoom>> fetchRooms(@Path("searchStrings") String searchStrings);

    // RoomFinder cordinates
    @GET("roomfinder/room/coordinates/{archId}")
    Call<RoomFinderCoordinate> fetchCoordinates(@Path("archId") String archId);

    // RoomFinder schedule
    @GET("roomfinder/room/scheduleById/{roomId}/{start}/{end}")
    Call<List<RoomFinderSchedule>> fetchSchedule(@Path("roomId") String archId,
                                                 @Path("start") String start, @Path("end") String end);

    @POST("feedback/")
    Call<FeedbackResult> sendFeedback(@Body Feedback feedback);

    @Multipart
    @POST("feedback/{id}/{image}/")
    Call<FeedbackResult> sendFeedbackImage(@Part MultipartBody.Part image, @Path("image") int imageNr, @Path("id") String feedbackId);

    @GET("mensen/")
    Observable<List<Cafeteria>> getCafeterias();

    @GET("kino/{lastId}")
    Flowable<List<Kino>> getKinos(@Path("lastId") String lastId);

    @GET("news/{lastNewsId}")
    Call<List<News>> getNews(@Path("lastNewsId") String lastNewsId);

    @GET("news/sources")
    Call<List<NewsSources>> getNewsSources();

    @GET("news/alert")
    Observable<NewsAlert> getNewsAlert();

    @GET("studyroom/list")
    Call<List<StudyRoomGroup>> getStudyRoomGroups();

    // TICKET SALE

    // Getting Event information

    @GET("event/list")
    Observable<List<Event>> getEvents();

    // Getting Ticket information
    @Headers("x-requires-verification: true")
    @POST("event/ticket/my")
    Observable<List<Ticket>> getTickets();

    @POST("event/ticket/{ticketID}")
    Call<Ticket> getTicket(@Path("ticketID") int ticketID, @Body TumCabeVerification verification);

    @GET("event/ticket/type/{eventID}")
    Observable<List<TicketType>> getTicketTypes(@Path("eventID") int eventID);

    // Ticket reservation
    @Headers("x-requires-verification: true")
    @POST("event/ticket/reserve/multiple")
    Call<TicketReservationResponse> reserveTicket(
            @Body TicketReservation data
    );

    // Ticket purchase
    @Headers("x-requires-verification: true")
    @POST("event/ticket/payment/stripe/purchase/multiple")
    Call<List<Ticket>> purchaseTicketStripe(
            @Body TicketPurchaseStripe data
    );

    @POST("event/ticket/payment/stripe/ephemeralkey")
    Call<HashMap<String, Object>> retrieveEphemeralKey(
            @Body EphimeralKey data
    );

    @GET("event/ticket/status/{event}")
    Single<List<TicketStatus>> getTicketStats(@Path("event") int event);

    // Update note
    @GET("updatenote/{version}")
    Call<UpdateNote> getUpdateNote(@Path("version") int version);

    // Opening Hours
    @GET("openingtimes/{language}")
    Call<List<Location>> getOpeningHours(@Path("language") String language);

}