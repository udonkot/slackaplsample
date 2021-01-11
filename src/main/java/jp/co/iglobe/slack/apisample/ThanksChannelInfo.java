package jp.co.iglobe.slack.apisample;


import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.api.ApiTestResponse;
import com.slack.api.model.Message;
import com.slack.api.model.User;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 「ありがとう_thanks_for_doing」チャンネルの投稿ユーザを表示
 */
public class ThanksChannelInfo {

    private static ApiTestResponse response;
    // トークンID取得
    private static final String SLACK_TOKEN = System.getenv("SLACK_TOKEN");
    // チャンネルID取得
    private static final String THANKS_CHANNEL = System.getenv("THANKS_CHANNEL");

    /**
     * メイン処理
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // slackインスタンス作成
        Slack slack = Slack.getInstance();
        // ロガー取得
        var logger = LoggerFactory.getLogger(ThanksChannelInfo.class.getName());

        /*
        // sendMessageTest
        ChatPostMessageResponse methods = slack.methods(SLACK_TOKEN).chatPostMessage(req ->
          req.channel("#bot_test")
            .text("testmessage from JavaAPI"));

         */

        try {
            // slackクライアント取得
            MethodsClient client = slack.methods(SLACK_TOKEN);

            // ユーザー情報取得
            Map<String, User> userMap = getUsersMap(client, SLACK_TOKEN);

            // チャンネル情報取得
            var result = client.conversationsHistory(r ->
                    r.token(SLACK_TOKEN).channel(THANKS_CHANNEL)
            );

            // メッセージ用リスト作成
            Optional<List<Message>> resultList = Optional.empty();
            // Postされたメッセージ取得
            resultList = Optional.ofNullable(result.getMessages());

            // orElseなので投稿なしなら空のリストを返す。
            List<Message> newList = resultList.orElse(Collections.emptyList());
            if(!newList.isEmpty()) {
                // 1件ずつ処理
                newList.stream().forEach(msg -> {
                    // ユーザ名取得
                    String userName = userMap.get(msg.getUser()).getName();
                    // Channel join等のメッセージ以外は判定対象外
                    if( msg.getSubtype() == null){
                        // コメント日を取得
                        String timestamp = msg.getTs().replace(".","").substring(0, 13);
                        Instant instant = Instant.ofEpochMilli(Long.parseLong(timestamp));
                        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

                        // 結果出力
                        System.out.println("user:" + userName + " date:" + ldt.getMonthValue() +"/" + ldt.getDayOfMonth());
                    }
                });
            } else {
                System.out.println("non message");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

//        ChatPostMessageRequest request = ChatPostMessageRequest.builder()
//
//        ChatPostMessageResponse response = methods.chatPostMessage(request);

    }

    /**
     * ユーザ情報を取得しマップに格納
     * 参考：https://api.slack.com/methods/users.list/code
     * @param client
     * @param token
     * @return
     * @throws IOException
     * @throws SlackApiException
     */
    private static Map<String, User> getUsersMap(MethodsClient client, String token) throws IOException, SlackApiException {
        //
        var usersListResponse = client.usersList(r -> {
            return r.token(token);
        });
        List<User> userList = usersListResponse.getMembers();

        Map<String, User> userMap = userList.stream().collect(
                Collectors.toMap(User::getId, user -> {
                    System.out.println("key:" + user.getId() + " val:" + user.getName());
                    return user;
                }));

        return userMap;
    }
}
