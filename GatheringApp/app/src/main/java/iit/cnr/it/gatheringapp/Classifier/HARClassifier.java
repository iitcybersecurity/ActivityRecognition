package iit.cnr.it.gatheringapp.Classifier;

import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HARClassifier {

    private static final int N_SAMPLES = 200;
    private static List<Float> x;
    private static List<Float> y;
    private static List<Float> z;
    private TensorFlowClassifier classifier;

    private float[] results;

    public HARClassifier(TensorFlowClassifier classifier){
        x = new ArrayList<>();
        y = new ArrayList<>();
        z = new ArrayList<>();

        this.classifier = classifier;
    }

    public void setElement(float x_value, float y_value, float z_value){
        x.add(x_value);
        y.add(y_value);
        z.add(z_value);
    }

    public float[] activityPrediction() {
        if (x.size() == N_SAMPLES && y.size() == N_SAMPLES && z.size() == N_SAMPLES) {
            List<Float> data = new ArrayList<>();
            data.addAll(x);
            data.addAll(y);
            data.addAll(z);

            results = classifier.predictProbabilities(toFloatArray(data));

            x.clear();
            y.clear();
            z.clear();

            return results;
        }
        return null;
    }

    private float[] toFloatArray(List<Float> list) {
        int i = 0;
        float[] array = new float[list.size()];

        for (Float f : list) {
            array[i++] = (f != null ? f : Float.NaN);
        }
        return array;
    }
}
