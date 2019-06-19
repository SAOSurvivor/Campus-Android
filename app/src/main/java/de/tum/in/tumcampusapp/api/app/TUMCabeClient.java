package de.tum.in.tumcampusapp.api.app;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.tum.in.tumcampusapp.api.app.interceptors.TumCabeVerificationInterceptor;
import de.tum.in.tumcampusapp.api.app.model.DeviceRegister;
import de.tum.in.tumcampusapp.api.app.model.DeviceUploadFcmToken;
import de.tum.in.tumcampusapp.api.app.model.ObfuscatedIdsUpload;
import de.tum.in.tumcampusapp.api.app.model.RealTumCabeVerificationProvider;
import de.tum.in.tumcampusapp.api.app.model.TUMCabeStatus;
import de.tum.in.tumcampusapp.api.app.model.TumCabeVerificationProvider;
import de.tum.in.tumcampusapp.api.app.model.UploadStatus;
import de.tum.in.tumcampusapp.component.other.locations.model.BuildingToGps;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.Feedback;
import de.tum.in.tumcampusapp.component.tumui.feedback.model.FeedbackResult;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderCoordinate;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderMap;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom;
import de.tum.in.tumcampusapp.component.tumui.roomfinder.model.RoomFinderSchedule;
import de.tum.in.tumcampusapp.component.ui.alarm.model.FcmNotification;
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
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Proxy class for Retrofit client to our API hosted @app.tum.de
 */
public final class TumCabeClient {

    private static TumCabeClient instance;

    private final TumCabeApiService service;

    private TumCabeClient(Context context) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateSerializer())
                .create();

        TumCabeVerificationProvider verificationProvider =
                new RealTumCabeVerificationProvider(context);

        TumCabeVerificationInterceptor verificationInterceptor =
                new TumCabeVerificationInterceptor(verificationProvider);

        OkHttpClient client = ApiHelper.getOkHttpClient(context);
        OkHttpClient modifiedClient = client.newBuilder()
                .addInterceptor(verificationInterceptor)
                .build();

        service = new Retrofit.Builder()
                .baseUrl("https://" + Const.API_HOSTNAME + "/Api/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(modifiedClient)
                .build()
                .create(TumCabeApiService.class);
    }

    public static synchronized TumCabeClient getInstance(Context context) {
        if (instance == null) {
            instance = new TumCabeClient(context.getApplicationContext());
        }
        return instance;
    }

    public void createRoom(ChatRoom chatRoom, Callback<ChatRoom> cb) {
        service.createRoom(chatRoom).enqueue(cb);
    }

    @Nullable
    public ChatRoom createRoom(ChatRoom chatRoom) {
        try {
            return service.createRoom(chatRoom).execute().body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    @Nullable
    public ChatRoom getChatRoom(int id) {
        try {
            return service.getChatRoom(id).execute().body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    @Nullable
    public ChatMember createMember(ChatMember chatMember) {
        try {
            return service.createMember(chatMember).execute().body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    public void leaveChatRoom(ChatRoom chatRoom, Callback<ChatRoom> cb) {
        service.leaveChatRoom(chatRoom.getId()).enqueue(cb);
    }

    public void addUserToChat(ChatRoom chatRoom, ChatMember member, Callback<ChatRoom> cb) {
        service.addUserToChat(chatRoom.getId(), member.getId()).enqueue(cb);
    }

    public Observable<ChatMessage> sendMessage(int roomId, ChatMessage chatMessage) {
        if (chatMessage.isNewMessage()) {
            return service.sendMessage(roomId, chatMessage);
        }

        return service.updateMessage(roomId, chatMessage.getId(), chatMessage);
    }

    public Observable<List<ChatMessage>> getMessages(int roomId, long messageId) {
        return service.getMessages(roomId, messageId);
    }

    public Observable<List<ChatMessage>> getNewMessages(int roomId) {
        return service.getNewMessages(roomId);
    }

    @Nullable
    public List<ChatRoom> getMemberRooms(int memberId) {
        try {
            return service.getMemberRooms(memberId).execute().body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    Observable<TUMCabeStatus> uploadObfuscatedIds(String lrzId, ObfuscatedIdsUpload ids) {
        return service.uploadObfuscatedIds(lrzId, ids);
    }

    @Nullable
    public FcmNotification getNotification(int notification) {
        try {
            return service.getNotification(notification).execute().body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    public void confirm(int notification) {
        try {
            service.confirm(notification).execute();
        } catch (IOException e) {
            Utils.log(e);
            Utils.log(e);
        }
    }

    void deviceRegister(DeviceRegister verification, Callback<TUMCabeStatus> cb) {
        service.deviceRegister(verification)
                .enqueue(cb);
    }

    @Nullable
    public TUMCabeStatus verifyKey() {
        try {
            return service.verifyKey().execute().body();
        } catch (IOException e) {
            Utils.log(e);
            Utils.log(e);
            return null;
        }
    }

    public void deviceUploadGcmToken(DeviceUploadFcmToken verification, Callback<TUMCabeStatus> cb) {
        service.deviceUploadGcmToken(verification)
                .enqueue(cb);
    }

    @Nullable
    public UploadStatus getUploadStatus(String lrzId) {
        try {
            return service.getUploadStatus(lrzId).execute().body();
        } catch (IOException e) {
            Utils.log(e);
            Utils.log(e);
            return null;
        }
    }

    @Nullable
    public List<BarrierfreeContact> getBarrierfreeContactList() {
        try {
            return service.getBarrierfreeContactList()
                    .execute()
                    .body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    @Nullable
    public List<BarrierfreeMoreInfo> getMoreInfoList() {
        try {
            return service.getMoreInfoList()
                    .execute()
                    .body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    public Call<List<RoomFinderRoom>> getListOfElevators() {
        return service.getListOfElevators();
    }

    public Call<List<RoomFinderRoom>> getListOfNearbyFacilities(String buildingId) {
        return service.getListOfNearbyFacilities(buildingId);
    }

    @Nullable
    public List<BuildingToGps> getBuilding2Gps() {
        try {
            return service.getBuilding2Gps()
                    .execute()
                    .body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    public Call<List<RoomFinderMap>> fetchAvailableMaps(final String archId) {
        return service.fetchAvailableMaps(ApiHelper.encodeUrl(archId));
    }

    @Nullable
    public List<RoomFinderRoom> fetchRooms(String searchStrings) {
        try {
            return service.fetchRooms(ApiHelper.encodeUrl(searchStrings))
                    .execute()
                    .body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    @Nullable
    public RoomFinderCoordinate fetchCoordinates(String archId) {
        try {
            return fetchRoomFinderCoordinates(archId).execute().body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    public Call<RoomFinderCoordinate> fetchRoomFinderCoordinates(String archId) {
        return service.fetchCoordinates(ApiHelper.encodeUrl(archId));
    }

    @Nullable
    public List<RoomFinderSchedule> fetchSchedule(String roomId, String start, String end) {
        try {
            return service.fetchSchedule(ApiHelper.encodeUrl(roomId),
                    ApiHelper.encodeUrl(start), ApiHelper.encodeUrl(end))
                    .execute()
                    .body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    public Call<FeedbackResult> sendFeedback(Feedback feedback) {
        return service.sendFeedback(feedback);
    }

    public List<Call<FeedbackResult>> sendFeedbackImages(Feedback feedback, String[] imagePaths) {
        List<Call<FeedbackResult>> calls = new ArrayList<>();
        for (int i = 0; i < imagePaths.length; i++) {
            File file = new File(imagePaths[i]);
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("feedback_image", i + ".png", reqFile);

            Call<FeedbackResult> call = service.sendFeedbackImage(body, i + 1, feedback.getId());
            calls.add(call);
        }
        return calls;
    }

    public void searchChatMember(String query, Callback<List<ChatMember>> callback) {
        service.searchMemberByName(query)
                .enqueue(callback);
    }

    public void getChatMemberByLrzId(String lrzId, Callback<ChatMember> callback) {
        service.getMember(lrzId)
                .enqueue(callback);
    }

    public Observable<List<Cafeteria>> getCafeterias() {
        return service.getCafeterias();
    }

    public Flowable<List<Kino>> getKinos(String lastId) {
        return service.getKinos(lastId);
    }

    @Nullable
    public List<News> getNews(String lastNewsId) {
        try {
            return service.getNews(lastNewsId)
                    .execute()
                    .body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    @Nullable
    public List<NewsSources> getNewsSources() {
        try {
            return service.getNewsSources()
                    .execute()
                    .body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    public Observable<NewsAlert> getNewsAlert() {
        return service.getNewsAlert();
    }

    public Call<List<StudyRoomGroup>> getStudyRoomGroups() {
        return service.getStudyRoomGroups();
    }

    // TICKET SALE

    // Getting event information
    public Observable<List<Event>> fetchEvents() {
        return service.getEvents();
    }

    // Getting ticket information

    public Observable<List<Ticket>> fetchTickets() {
        return service.getTickets();
    }

    public Observable<List<TicketType>> fetchTicketTypes(int eventID) {
        return service.getTicketTypes(eventID);
    }

    // Ticket reservation

    public void reserveTicket(TicketReservation reservation,
                              Callback<TicketReservationResponse> cb) {
        service.reserveTicket(reservation).enqueue(cb);
    }

    // Ticket purchase

    public void purchaseTickets(List<Integer> ticketIds,
                                @NonNull String token,
                                @NonNull String customerName,
                                Callback<List<Ticket>> cb) {
        TicketPurchaseStripe purchase = new TicketPurchaseStripe(ticketIds, token, customerName);
        service.purchaseTicketStripe(purchase).enqueue(cb);
    }

    public void retrieveEphemeralKey(String apiVersion, Callback<HashMap<String, Object>> cb) {
        EphimeralKey key = new EphimeralKey(apiVersion);
        service.retrieveEphemeralKey(key).enqueue(cb);
    }

    public Single<List<TicketStatus>> fetchTicketStats(int event) {
        return service.getTicketStats(event);
    }

    @Nullable
    public UpdateNote getUpdateNote(int version) {
        try {
            return service.getUpdateNote(version).execute().body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

    @Nullable
    public List<Location> fetchOpeningHours(String language) {
        try {
            return service.getOpeningHours(language)
                    .execute()
                    .body();
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }
    }

}
