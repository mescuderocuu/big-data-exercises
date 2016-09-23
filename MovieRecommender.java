package nearsoft.academy.bigdata.recommendation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {
    String PATH;
    private HashBiMap<String, Integer> productMap= HashBiMap.create ();
    private HashBiMap <String, Integer> userMap= HashBiMap.create ();



    public MovieRecommender(String path){
        PATH=path;
    }

    public int getTotalReviews() {
        int count = createCsv();
        return count;
    }

    public int getTotalProducts() {
        return productMap.size();
    }

    public int getTotalUsers() {
        return userMap.size();
    }

    public List<String> getRecommendationsForUser(String idUser) {
        try {
            DataModel model = new FileDataModel(new File("/home/jonathan/Downloads/big-data-exercises-master/prueba.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            int idUser2 = userMap.get(idUser);
            List <RecommendedItem> recommendations = recommender.recommend(idUser2,3);
            List <String> products = new ArrayList<String>();
            for (RecommendedItem recommendation : recommendations) {
                products.add(getProductId(recommendation.getItemID()));
            }
            return products;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TasteException e) {
            e.printStackTrace();
        }
        return null;

    }

    public int  createCsv(){
        FileWriter fw;
        BufferedWriter bw = null;
        int count=0;
        try {
            fw = new FileWriter("prueba.csv");
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int keyU=1;
        int keyP=1;
        try {
            InputStream fis = new FileInputStream(PATH);
            InputStream gzs = new GZIPInputStream(fis);
            Reader decoder = new InputStreamReader(gzs, "UTF-8");
            BufferedReader br = new BufferedReader(decoder);
            String line;
            int idP=1, idU=1;
            String csv;
            while ((line = br.readLine()) != null)   {
                if(line.length()>16) {
                    if (line.substring(0, 17).equals("product/productId")) {
                        if (!productMap.containsKey(line.substring(19))) {
                            productMap.put(line.substring(19), keyP);
                            idP = keyP;
                            keyP++;
                        } else {
                            idP = productMap.get(line.substring(19));
                        }
                    } else if (line.substring(0, 13).equals("review/userId")) {
                        if (!userMap.containsKey(line.substring(15))) {
                            userMap.put(line.substring(15), keyU);
                            idU = keyU;
                            keyU++;
                        } else {
                            idU = userMap.get(line.substring(15));
                        }
                    } else if (line.substring(0, 12).equals("review/score")) {
                        csv = String.format("%s,%s,%s\n", idU, idP, line.substring(14));
                        bw.write(csv);
                        count++;
                    }
                }
            }
            bw.close();
            fis.close();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    public String getProductId(long idValue){
        return productMap.inverse().get((int)(long)idValue);
    }
}
