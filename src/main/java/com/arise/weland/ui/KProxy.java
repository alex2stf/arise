package com.arise.weland.ui;

import com.arise.cargo.management.DependencyManager;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.*;
import com.arise.core.tools.ReflectUtil.ClazzHelper;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.net.URLClassLoader;
import java.util.*;

import static com.arise.cargo.management.DependencyManager.withJar;
import static com.arise.core.tools.ReflectUtil.getMethod;

//import org.apache.kafka.clients.admin.Config;

public class KProxy {


    private static final String KAFKA_CLIENT_JAR = "KAFKA_CLIENT";
    private final Properties props;
//    KafkaConsumer kafkaConsumer;
    Object kafkaConsumer;
    Map<String, String> connectionProps = new HashMap<>();
    List<String> brokers = new ArrayList<>();
    List<String> topics = new ArrayList<>();
    private static final Mole log = Mole.getInstance(KProxy.class);

    public KProxy(final Properties props){
        this.props = props;
        DependencyManager.withJar(KAFKA_CLIENT_JAR, new Handler<URLClassLoader>() {
            @Override
            public void handle(URLClassLoader urlClassLoader) {
                kafkaConsumer = ReflectUtil.getClass("org.apache.kafka.clients.consumer.KafkaConsumer", true, urlClassLoader)
                        .getConstructor(Properties.class)
                        .newInstance(props);
                if (null != kafkaConsumer) {
                    log.info("Successfully created kafkaConsumer instance");
                }

            }
        });


   }

    public void scanConsumer(){
        stopRead();
        topics = new ArrayList<>();
        connectionProps = new HashMap<>();
        readAdminDetails();
        scanConsumer(kafkaConsumer, topics, connectionProps);

    }

    private void readAdminDetails() {

        withAdminClient(new Handler<Tuple2<URLClassLoader, Object>>() {
            @Override
            public void handle(Tuple2<URLClassLoader, Object> data) {
                Object admin = data.second();
                Object cluster = getMethod(admin, "describeCluster").call();
                Object nodes = getMethod(cluster, "nodes").call();
                Collection<Object> result = getMethod(nodes, "get").callForCollection();
                for(Object o: result)  {
                    String id = getMethod(o, "idString").callForString();
                    if (!StringUtil.hasText(id)){
                        id = getMethod(o, "id").call() + "";
                    }
                    brokers.add(id);
                }
            }
        });
//        AdminClient adminClient = AdminClient.create(props);
////        adminClient.deleteTopics()
//
//        adminClient.describeConfigs(Collections.singleton( new ConfigResource(ConfigResource.Type.BROKER, "0"))).all().get().forEach(new BiConsumer<ConfigResource, Config>() {
//            @Override
//            public void accept(ConfigResource configResource, Config config) {
//                System.out.println(config);
//                System.out.println(configResource);
//            }
//        });
//


//        adminClient.listTransactions().all().get()
//        adminClient.listTransactions().all().get().forEach(new Consumer<TransactionListing>() {
//            @Override
//            public void accept(TransactionListing transactionListing) {
//                transactionListing.transactionalId();
//                transactionListing.producerId();
//                transactionListing.state();
//            }
//        });
//        adminClient.describeCluster().nodes().get().forEach(new Consumer<Node>() {
//            @Override
//            public void accept(Node node) {
//
//            }
//        });
//        adminClient.listConsumerGroups().all().get().forEach(new Consumer<ConsumerGroupListing>() {
//            @Override
//            public void accept(ConsumerGroupListing consumerGroupListing) {
//                System.out.println(consumerGroupListing.groupId());
//                System.out.println(consumerGroupListing.isSimpleConsumerGroup());
//                System.out.println(consumerGroupListing.state().get());
//            }
//        });
//
//        adminClient.listTopics().listings().get().forEach(new Consumer<TopicListing>() {
//            @Override
//            public void accept(TopicListing topicListing) {
//                System.out.println(topicListing.topicId());
////                topicListing.topicId()
//            }
//        });


    }


    public DefaultTreeModel getTreeModel(){
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        if (topics.size() > 0) {
            DefaultMutableTreeNode tNode = new DefaultMutableTreeNode("topics");
            root.add(tNode);

            for (String t : topics) {
                tNode.add(new DefaultMutableTreeNode(t));
            }
        }

        if (brokers.size() > 0){
            DefaultMutableTreeNode rNode = new DefaultMutableTreeNode("brokers");
            root.add(rNode);

            for (String t : brokers) {
                rNode.add(new DefaultMutableTreeNode(t));
            }
        }
        return new DefaultTreeModel(root);
    }

    Thread readThread;
    boolean reads[] = new boolean[]{false};

    public  boolean isReading(){
        return reads[0];
    }

    public void stopRead(){
        reads[0] = false;
    }

    public void readTopic(final String t, final Handler<Map<String, Object>> h){

        readThread = ThreadUtil.startThread(new Runnable() {
            @Override
            public void run() {
                reads[0] = true;
                readConsumer( kafkaConsumer, t, h, reads);
            }
        }, "read-topic-" + t);
    }


    private static void readConsumer(Object kConsumer, String topic, Handler<Map<String, Object>> h, boolean[] read){
        ReflectUtil.getMethod(kConsumer, "subscribe", Collection.class).call(
                Arrays.asList(topic)
        );
        while (read[0]) {
            Class durationClz = ReflectUtil.getClassByName("java.time.Duration");
            Iterable records;
            if (durationClz != null){
                records = (Iterable) ReflectUtil.getMethod( kConsumer, "poll", durationClz).call(
                        ReflectUtil.getStaticMethod(durationClz, "ofMillis", long.class).call(1000)
                );
                log.once(Mole.Bag.INFO, "Fetching records  using Duration.class");
            } else {
                records = (Iterable) ReflectUtil.getMethod( kConsumer, "poll", long.class).call(1000);
                log.once(Mole.Bag.INFO, "Fetching records  using long.class");
            }

            for (Object record : records) {

                Map<String, Object> d = new HashMap<>();
                d.put("key", getMethod(record, "key").call());
                d.put("value", getMethod(record, "value").call());
                d.put("offset", getMethod(record, "offset").call());
                d.put("timestamp", getMethod(record, "timestamp").call());
                d.put("timestampType", getMethod(record, "timestampType").call());
                d.put("partition", getMethod(record, "partition").call());
//                ConsumerRecord record1 = (ConsumerRecord) record;
//                record1.headers().forEach(new Consumer<Header>() {
//                    @Override
//                    public void accept(Header header) {
//                        header.key()
//                    }
//                });
//                record1.headers()
//                record1.headers()
                h.handle(d);

            }
        }

    }


    private static void scanConsumer(Object kConsumer,
                                     List<String> topics,
                                     Map<String, String> connectionProps){
          Object obj = getMethod(kConsumer, "listTopics").call();
          Object meta = getMethod(kConsumer, "groupMetadata").call();
          String groupId = getMethod(meta, "groupId").callForString();
          String memberId = getMethod(meta, "memberId").callForString();


          connectionProps.put("groupId", groupId + "");
          if (StringUtil.hasText(memberId)) {
              connectionProps.put("memberId", memberId + "");
          }

          if (obj instanceof Map){
              Map<String, Object> map = (Map<String, Object>) obj;
              connectionProps.put("topicsCount", map.entrySet().size() + "");
              for (Map.Entry<String, Object> e: map.entrySet()){
                  topics.add(e.getKey());
              }
          }

    }





    public String getTopicDetails(final String topicName) {
        return getDetails("TOPIC", topicName);
    }

    private Map<String, String> detailsCache = new HashMap<>();

    public String getDetails(final String enumval, final String id) {

        String idx = enumval + id;

        if (detailsCache.containsKey(idx)){
            return detailsCache.get(idx);
        }
        final StringBuilder sb = new StringBuilder();
        withAdminClient(new Handler<Tuple2<URLClassLoader, Object>>() {
            @Override
            public void handle(Tuple2<URLClassLoader, Object> data) {
                ClazzHelper rcc = ReflectUtil.getClass("org.apache.kafka.common.config.ConfigResource", true, data.first());
                ClazzHelper typeClass = rcc.getNestedClass("org.apache.kafka.common.config.ConfigResource$Type");
                Object type = typeClass.getEnumValue(enumval);
                Object configResource = ReflectUtil.getClass("org.apache.kafka.common.config.ConfigResource", true, data.first())
                        .getConstructor(typeClass.getClazz(), String.class).newInstance(type, id);
                System.out.println(configResource);

               Object res1 = getMethod(data.second(), "describeConfigs", Collection.class)
                        .call(Collections.singletonList(configResource));
               Object res2 = getMethod(res1, "all").call();
               Map res3 = getMethod(res2, "get").callForMap();


                  /**
                for(Map.Entry<Object, Object> e: res3.entrySet()) {
                    Object o = e.getKey();
                    Object config = e.getValue();
                }
               res3.forEach(new BiConsumer<Object, Object>() {
                   @Override
                   public void accept(Object o, Object config) {

                       Config cfg = (Config) config;
                       Collection entries = getMethod(cfg, "entries").callForCollection();
                       entries.forEach(new Consumer() {
                           @Override
                           public void accept(Object o) {
                               String name = getMethod(o, "name").callForString();
                               String value = getMethod(o, "value").callForString();
                               sb.append(name).append("=").append(value).append("\n");
                           }
                       });
                   }
               });
                   **/
                //TDO Fix this to java 7
            }
        });

        detailsCache.put(idx, sb.toString());

        return detailsCache.get(idx);
    }

    public String getBrokerDetails(String id) {
        return getDetails("BROKER", id);
    }

    public String getConnectionDetails() {

        return StringUtil.join(connectionProps);
    }


    private void withAdminClient(final Handler<Tuple2<URLClassLoader, Object>> handler){
        withJar(KAFKA_CLIENT_JAR, new Handler<URLClassLoader>() {

            @Override
            public void handle(URLClassLoader clzLoader) {
                Object client = ReflectUtil.getClass("org.apache.kafka.clients.admin.AdminClient", true, clzLoader)
                        .getStaticMethod("create", Properties.class).call(props);

                handler.handle(new Tuple2<>(clzLoader, client));
                Util.close(client);
            }
        });
    }

    public void createTopic(final String topicName, final Properties cp, final Handler<String> handler) {

        final int numPartitions = MapUtil.getInt(cp, "num.partitions", 1);
        final short replicationFactor = MapUtil.getShort(cp, "replication.factor", (short)1);

        withAdminClient(new Handler<Tuple2<URLClassLoader, Object>>() {
            @Override
            public void handle(Tuple2<URLClassLoader, Object> data) {
                try {
                    Object newTopic = ReflectUtil.getClass("org.apache.kafka.clients.admin.NewTopic", true, data.first())
                            .getConstructor(String.class, int.class, short.class)
                            .newInstance(topicName, numPartitions, replicationFactor);

                    Object result = getMethod(data.second(), "createTopics", Collection.class)
                            .call(Arrays.asList(newTopic));

                    Object future = getMethod(result, "all").call();
                    getMethod(future, "get").call();
                    handler.handle("Successfully created " + topicName);
                } catch (Exception e){
                    Mole.getInstance(KProxy.class).error(e);
                    handler.handle("Error creating topic " + topicName + "\n\n" + StringUtil.dump(e));
                }
            }
        });



    }


}
