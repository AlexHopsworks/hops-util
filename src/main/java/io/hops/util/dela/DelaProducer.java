/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.hops.util.dela;

import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import io.hops.util.Hops;
import io.hops.util.HopsProcess;
import io.hops.util.HopsProcessType;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Hops Dela wrapper for a Kafka producer.
 *
 */
public class DelaProducer extends HopsProcess {

  private static final Logger LOGGER = Logger.getLogger(DelaProducer.class.getName());

  private final KafkaProducer<String, byte[]> producer;
  private final Injection<GenericRecord, byte[]> recordInjection;

  public DelaProducer(String topic, Schema schema, long lingerDelay) {
    super(HopsProcessType.PRODUCER, topic, schema);
    Properties props = Hops.getKafkaProperties().defaultProps();
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "DelaProducer");
    props.put(ProducerConfig.LINGER_MS_CONFIG, lingerDelay);
    producer = new KafkaProducer<>(props);
    recordInjection = GenericAvroCodecs.toBinary(schema);
  }

  public void produce(Map<String, Object> messageFields) {
    //create the avro message
    GenericData.Record avroRecord = new GenericData.Record(schema);
    for (Map.Entry<String, Object> message : messageFields.entrySet()) {
      //TODO: Check that messageFields are in avro record
      avroRecord.put(message.getKey(), message.getValue());
    }
    produce(avroRecord);
  }

  public void produce(GenericRecord avroRecord) {
    byte[] bytes = recordInjection.apply(avroRecord);
    produce(bytes);
  }

  public void produce(byte[] byteRecord) {
    ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, byteRecord);
    producer.send(record);
  }

  public byte[] prepareRecord(GenericRecord avroRecord) {
    byte[] bytes = recordInjection.apply(avroRecord);
    return bytes;
  }

  @Override
  public void close() {
    producer.close();
  }
}
