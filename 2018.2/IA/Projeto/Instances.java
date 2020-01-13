// Decompiled using: fernflower
// Took: 1614ms

package weka.core;

import weka.core.converters.ConverterUtils;
import java.io.FileReader;
import weka.experiment.Stats;
import java.util.Random;
import java.util.Enumeration;
import java.util.HashSet;
import java.io.IOException;
import weka.core.converters.ArffLoader;
import java.io.Reader;
import java.io.Serializable;

public class Instances implements Serializable, RevisionHandler {
    static final long serialVersionUID = -19412345060742748L;
    public static final String FILE_EXTENSION = ".arff";
    public static final String SERIALIZED_OBJ_FILE_EXTENSION = ".bsi";
    public static final String ARFF_RELATION = "@relation";
    public static final String ARFF_DATA = "@data";
    protected String m_RelationName;
    protected FastVector m_Attributes;
    protected FastVector m_Instances;
    protected int m_ClassIndex;
    protected int m_Lines;
    
    public Instances(final Reader reader) throws IOException {
        super();
        this.m_Lines = 0;
        final ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
        final Instances dataset = arff.getData();
        this.initialize(dataset, dataset.numInstances());
        dataset.copyInstances(0, this, dataset.numInstances());
        this.compactify();
    }
    
    public Instances(final Reader reader, final int capacity) throws IOException {
        super();
        this.m_Lines = 0;
        final ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader, 0);
        final Instances header = arff.getStructure();
        this.initialize(header, capacity);
        this.m_Lines = arff.getLineNo();
    }
    
    public Instances(final Instances dataset) {
        this(dataset, dataset.numInstances());
        dataset.copyInstances(0, this, dataset.numInstances());
    }
    
    public Instances(final Instances dataset, final int capacity) {
        super();
        this.m_Lines = 0;
        this.initialize(dataset, capacity);
    }
    
    protected void initialize(final Instances dataset, int capacity) {
        if (capacity < 0) {
            capacity = 0;
        }
        this.m_ClassIndex = dataset.m_ClassIndex;
        this.m_RelationName = dataset.m_RelationName;
        this.m_Attributes = dataset.m_Attributes;
        this.m_Instances = new FastVector(capacity);
    }
    
    public Instances(final Instances source, final int first, final int toCopy) {
        this(source, toCopy);
        if (first < 0 || first + toCopy > source.numInstances()) {
            throw new IllegalArgumentException("Parameters first and/or toCopy out of range");
        }
        source.copyInstances(first, this, toCopy);
    }
    
    public Instances(final String name, final FastVector attInfo, final int capacity) {
        super();
        this.m_Lines = 0;
        final HashSet<String> names = new HashSet<String>();
        final StringBuffer nonUniqueNames = new StringBuffer();
        for (int i = 0; i < attInfo.size(); ++i) {
            if (names.contains(((Attribute)attInfo.elementAt(i)).name())) {
                nonUniqueNames.append("'" + ((Attribute)attInfo.elementAt(i)).name() + "' ");
            }
            names.add(((Attribute)attInfo.elementAt(i)).name());
        }
        if (names.size() != attInfo.size()) {
            throw new IllegalArgumentException("Attribute names are not unique! Causes: " + nonUniqueNames.toString());
        }
        names.clear();
        this.m_RelationName = name;
        this.m_ClassIndex = -1;
        this.m_Attributes = attInfo;
        for (int i = 0; i < this.numAttributes(); ++i) {
            this.attribute(i).setIndex(i);
        }
        this.m_Instances = new FastVector(capacity);
    }
    
    public Instances stringFreeStructure() {
        final FastVector newAtts = new FastVector();
        for (int i = 0; i < this.m_Attributes.size(); ++i) {
            final Attribute att = (Attribute)this.m_Attributes.elementAt(i);
            if (att.type() == 2) {
                newAtts.addElement((Object)new Attribute(att.name(), (FastVector)null, i));
            }
            else if (att.type() == 4) {
                newAtts.addElement((Object)new Attribute(att.name(), new Instances(att.relation(), 0), i));
            }
        }
        if (newAtts.size() == 0) {
            return new Instances(this, 0);
        }
        final FastVector atts = (FastVector)this.m_Attributes.copy();
        for (int j = 0; j < newAtts.size(); ++j) {
            atts.setElementAt(newAtts.elementAt(j), ((Attribute)newAtts.elementAt(j)).index());
        }
        final Instances result = new Instances(this, 0);
        result.m_Attributes = atts;
        return result;
    }
    
    public void add(final Instance instance) {
        final Instance newInstance = (Instance)instance.copy();
        newInstance.setDataset(this);
        this.m_Instances.addElement((Object)newInstance);
    }
    
    public Attribute attribute(final int index) {
        return (Attribute)this.m_Attributes.elementAt(index);
    }
    
    public Attribute attribute(final String name) {
        for (int i = 0; i < this.numAttributes(); ++i) {
            if (this.attribute(i).name().equals(name)) {
                return this.attribute(i);
            }
        }
        return null;
    }
    
    public boolean checkForAttributeType(final int attType) {
        int i = 0;
        while (i < this.m_Attributes.size()) {
            if (this.attribute(i++).type() == attType) {
                return true;
            }
        }
        return false;
    }
    
    public boolean checkForStringAttributes() {
        return this.checkForAttributeType(2);
    }
    
    public boolean checkInstance(final Instance instance) {
        if (instance.numAttributes() != this.numAttributes()) {
            return false;
        }
        for (int i = 0; i < this.numAttributes(); ++i) {
            if (!instance.isMissing(i)) {
                if (this.attribute(i).isNominal() || this.attribute(i).isString()) {
                    if (!Utils.eq(instance.value(i), (double)(int)instance.value(i))) {
                        return false;
                    }
                    if (Utils.sm(instance.value(i), 0.0) || Utils.gr(instance.value(i), (double)this.attribute(i).numValues())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public Attribute classAttribute() {
        if (this.m_ClassIndex < 0) {
            throw new UnassignedClassException("Class index is negative (not set)!");
        }
        return this.attribute(this.m_ClassIndex);
    }
    
    public int classIndex() {
        return this.m_ClassIndex;
    }
    
    public void compactify() {
        this.m_Instances.trimToSize();
    }
    
    public void delete() {
        this.m_Instances = new FastVector();
    }
    
    public void delete(final int index) {
        this.m_Instances.removeElementAt(index);
    }
    
    public void deleteAttributeAt(final int position) {
        if (position < 0 || position >= this.m_Attributes.size()) {
            throw new IllegalArgumentException("Index out of range");
        }
        if (position == this.m_ClassIndex) {
            throw new IllegalArgumentException("Can't delete class attribute");
        }
        this.freshAttributeInfo();
        if (this.m_ClassIndex > position) {
            --this.m_ClassIndex;
        }
        this.m_Attributes.removeElementAt(position);
        for (int i = position; i < this.m_Attributes.size(); ++i) {
            final Attribute current = (Attribute)this.m_Attributes.elementAt(i);
            current.setIndex(current.index() - 1);
        }
        for (int i = 0; i < this.numInstances(); ++i) {
            this.instance(i).forceDeleteAttributeAt(position);
        }
    }
    
    public void deleteAttributeType(final int attType) {
        int i = 0;
        while (i < this.m_Attributes.size()) {
            if (this.attribute(i).type() == attType) {
                this.deleteAttributeAt(i);
            }
            else {
                ++i;
            }
        }
    }
    
    public void deleteStringAttributes() {
        this.deleteAttributeType(2);
    }
    
    public void deleteWithMissing(final int attIndex) {
        final FastVector newInstances = new FastVector(this.numInstances());
        for (int i = 0; i < this.numInstances(); ++i) {
            if (!this.instance(i).isMissing(attIndex)) {
                newInstances.addElement((Object)this.instance(i));
            }
        }
        this.m_Instances = newInstances;
    }
    
    public void deleteWithMissing(final Attribute att) {
        this.deleteWithMissing(att.index());
    }
    
    public void deleteWithMissingClass() {
        if (this.m_ClassIndex < 0) {
            throw new UnassignedClassException("Class index is negative (not set)!");
        }
        this.deleteWithMissing(this.m_ClassIndex);
    }
    
    public Enumeration enumerateAttributes() {
        return this.m_Attributes.elements(this.m_ClassIndex);
    }
    
    public Enumeration enumerateInstances() {
        return this.m_Instances.elements();
    }
    
    public boolean equalHeaders(final Instances dataset) {
        if (this.m_ClassIndex != dataset.m_ClassIndex) {
            return false;
        }
        if (this.m_Attributes.size() != dataset.m_Attributes.size()) {
            return false;
        }
        for (int i = 0; i < this.m_Attributes.size(); ++i) {
            if (!this.attribute(i).equals((Object)dataset.attribute(i))) {
                return false;
            }
        }
        return true;
    }
    
    public Instance firstInstance() {
        return (Instance)this.m_Instances.firstElement();
    }
    
    public Random getRandomNumberGenerator(final long seed) {
        final Random r = new Random(seed);
        r.setSeed(this.instance(r.nextInt(this.numInstances())).toStringNoWeight().hashCode() + seed);
        return r;
    }
    
    public void insertAttributeAt(Attribute att, final int position) {
        if (position < 0 || position > this.m_Attributes.size()) {
            throw new IllegalArgumentException("Index out of range");
        }
        if (this.attribute(att.name()) != null) {
            throw new IllegalArgumentException("Attribute name '" + att.name() + "' already in use at position #" + this.attribute(att.name()).index());
        }
        att = (Attribute)att.copy();
        this.freshAttributeInfo();
        att.setIndex(position);
        this.m_Attributes.insertElementAt((Object)att, position);
        for (int i = position + 1; i < this.m_Attributes.size(); ++i) {
            final Attribute current = (Attribute)this.m_Attributes.elementAt(i);
            current.setIndex(current.index() + 1);
        }
        for (int i = 0; i < this.numInstances(); ++i) {
            this.instance(i).forceInsertAttributeAt(position);
        }
        if (this.m_ClassIndex >= position) {
            ++this.m_ClassIndex;
        }
    }
    
    public Instance instance(final int index) {
        return (Instance)this.m_Instances.elementAt(index);
    }
    
    public double kthSmallestValue(final Attribute att, final int k) {
        return this.kthSmallestValue(att.index(), k);
    }
    
    public double kthSmallestValue(final int attIndex, final int k) {
        if (!this.attribute(attIndex).isNumeric()) {
            throw new IllegalArgumentException("Instances: attribute must be numeric to compute kth-smallest value.");
        }
        if (k < 1 || k > this.numInstances()) {
            throw new IllegalArgumentException("Instances: value for k for computing kth-smallest value too large.");
        }
        final double[] vals = new double[this.numInstances()];
        for (int i = 0; i < vals.length; ++i) {
            final double val = this.instance(i).value(attIndex);
            if (Instance.isMissingValue(val)) {
                vals[i] = Double.MAX_VALUE;
            }
            else {
                vals[i] = val;
            }
        }
        return Utils.kthSmallestValue(vals, k);
    }
    
    public Instance lastInstance() {
        return (Instance)this.m_Instances.lastElement();
    }
    
    public double meanOrMode(final int attIndex) {
        if (this.attribute(attIndex).isNumeric()) {
            double result;
            double found = result = 0.0;
            for (int j = 0; j < this.numInstances(); ++j) {
                if (!this.instance(j).isMissing(attIndex)) {
                    found += this.instance(j).weight();
                    result += this.instance(j).weight() * this.instance(j).value(attIndex);
                }
            }
            if (found <= 0.0) {
                return 0.0;
            }
            return result / found;
        }
        else {
            if (this.attribute(attIndex).isNominal()) {
                final int[] counts = new int[this.attribute(attIndex).numValues()];
                for (int j = 0; j < this.numInstances(); ++j) {
                    if (!this.instance(j).isMissing(attIndex)) {
                        final int[] array = counts;
                        final int n = (int)this.instance(j).value(attIndex);
                        array[n] += (int)this.instance(j).weight();
                    }
                }
                return Utils.maxIndex(counts);
            }
            return 0.0;
        }
    }
    
    public double meanOrMode(final Attribute att) {
        return this.meanOrMode(att.index());
    }
    
    public int numAttributes() {
        return this.m_Attributes.size();
    }
    
    public int numClasses() {
        if (this.m_ClassIndex < 0) {
            throw new UnassignedClassException("Class index is negative (not set)!");
        }
        if (!this.classAttribute().isNominal()) {
            return 1;
        }
        return this.classAttribute().numValues();
    }
    
    public int numDistinctValues(final int attIndex) {
        if (this.attribute(attIndex).isNumeric()) {
            final double[] attVals = this.attributeToDoubleArray(attIndex);
            final int[] sorted = Utils.sort(attVals);
            double prev = 0.0;
            int counter = 0;
            for (int i = 0; i < sorted.length; ++i) {
                final Instance current = this.instance(sorted[i]);
                if (current.isMissing(attIndex)) {
                    break;
                }
                if (i == 0 || current.value(attIndex) > prev) {
                    prev = current.value(attIndex);
                    ++counter;
                }
            }
            return counter;
        }
        return this.attribute(attIndex).numValues();
    }
    
    public int numDistinctValues(final Attribute att) {
        return this.numDistinctValues(att.index());
    }
    
    public int numInstances() {
        return this.m_Instances.size();
    }
    
    public void randomize(final Random random) {
        for (int j = this.numInstances() - 1; j > 0; --j) {
            this.swap(j, random.nextInt(j + 1));
        }
    }
    
    @Deprecated
    public boolean readInstance(final Reader reader) throws IOException {
        final ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader, this, this.m_Lines, 1);
        final Instance inst = arff.readInstance(arff.getData(), false);
        this.m_Lines = arff.getLineNo();
        if (inst != null) {
            this.add(inst);
            return true;
        }
        return false;
    }
    
    public String relationName() {
        return this.m_RelationName;
    }
    
    public void renameAttribute(final int att, final String name) {
        for (int i = 0; i < this.numAttributes(); ++i) {
            if (i != att) {
                if (this.attribute(i).name().equals(name)) {
                    throw new IllegalArgumentException("Attribute name '" + name + "' already present at position #" + i);
                }
            }
        }
        final Attribute newAtt = this.attribute(att).copy(name);
        final FastVector newVec = new FastVector(this.numAttributes());
        for (int j = 0; j < this.numAttributes(); ++j) {
            if (j == att) {
                newVec.addElement((Object)newAtt);
            }
            else {
                newVec.addElement((Object)this.attribute(j));
            }
        }
        this.m_Attributes = newVec;
    }
    
    public void renameAttribute(final Attribute att, final String name) {
        this.renameAttribute(att.index(), name);
    }
    
    public void renameAttributeValue(final int att, final int val, final String name) {
        final Attribute newAtt = (Attribute)this.attribute(att).copy();
        final FastVector newVec = new FastVector(this.numAttributes());
        newAtt.setValue(val, name);
        for (int i = 0; i < this.numAttributes(); ++i) {
            if (i == att) {
                newVec.addElement((Object)newAtt);
            }
            else {
                newVec.addElement((Object)this.attribute(i));
            }
        }
        this.m_Attributes = newVec;
    }
    
    public void renameAttributeValue(final Attribute att, final String val, final String name) {
        final int v = att.indexOfValue(val);
        if (v == -1) {
            throw new IllegalArgumentException(val + " not found");
        }
        this.renameAttributeValue(att.index(), v, name);
    }
    
    public Instances resample(final Random random) {
        final Instances newData = new Instances(this, this.numInstances());
        while (newData.numInstances() < this.numInstances()) {
            newData.add(this.instance(random.nextInt(this.numInstances())));
        }
        return newData;
    }
    
    public Instances resampleWithWeights(final Random random) {
        final double[] weights = new double[this.numInstances()];
        for (int i = 0; i < weights.length; ++i) {
            weights[i] = this.instance(i).weight();
        }
        return this.resampleWithWeights(random, weights);
    }
    
    public Instances resampleWithWeights(final Random random, final boolean[] sampled) {
        final double[] weights = new double[this.numInstances()];
        for (int i = 0; i < weights.length; ++i) {
            weights[i] = this.instance(i).weight();
        }
        return this.resampleWithWeights(random, weights, sampled);
    }
    
    public Instances resampleWithWeights(final Random random, final double[] weights) {
        return this.resampleWithWeights(random, weights, null);
    }
    
    public Instances resampleWithWeights(final Random random, final double[] weights, final boolean[] sampled) {
        if (weights.length != this.numInstances()) {
            throw new IllegalArgumentException("weights.length != numInstances.");
        }
        final Instances newData = new Instances(this, this.numInstances());
        if (this.numInstances() == 0) {
            return newData;
        }
        final double[] P = new double[weights.length];
        System.arraycopy(weights, 0, P, 0, weights.length);
        Utils.normalize(P);
        final double[] Q = new double[weights.length];
        final int[] A = new int[weights.length];
        final int[] W = new int[weights.length];
        final int M = weights.length;
        int NN = -1;
        int NP = M;
        for (int I = 0; I < M; ++I) {
            if (P[I] < 0.0) {
                throw new IllegalArgumentException("Weights have to be positive.");
            }
            Q[I] = M * P[I];
            if (Q[I] < 1.0) {
                W[++NN] = I;
            }
            else {
                W[--NP] = I;
            }
        }
        if (NN > -1 && NP < M) {
            for (int S = 0; S < M - 1; ++S) {
                final int I2 = W[S];
                final int J = W[NP];
                A[I2] = J;
                final double[] array = Q;
                final int n = J;
                array[n] += Q[I2] - 1.0;
                if (Q[J] < 1.0) {
                    ++NP;
                }
                if (NP >= M) {
                    break;
                }
            }
        }
        for (int I = 0; I < M; ++I) {
            final double[] array2 = Q;
            final int n2 = I;
            array2[n2] += I;
        }
        for (int i = 0; i < this.numInstances(); ++i) {
            final double U = M * random.nextDouble();
            final int I3 = (int)U;
            int ALRV;
            if (U < Q[I3]) {
                ALRV = I3;
            }
            else {
                ALRV = A[I3];
            }
            newData.add(this.instance(ALRV));
            if (sampled != null) {
                sampled[ALRV] = true;
            }
            newData.instance(newData.numInstances() - 1).setWeight(1.0);
        }
        return newData;
    }
    
    public void setClass(final Attribute att) {
        this.m_ClassIndex = att.index();
    }
    
    public void setClassIndex(final int classIndex) {
        if (classIndex >= this.numAttributes()) {
            throw new IllegalArgumentException("Invalid class index: " + classIndex);
        }
        this.m_ClassIndex = classIndex;
    }
    
    public void setRelationName(final String newName) {
        this.m_RelationName = newName;
    }
    
    public void sort(final int attIndex) {
        final double[] vals = new double[this.numInstances()];
        for (int i = 0; i < vals.length; ++i) {
            final double val = this.instance(i).value(attIndex);
            if (Instance.isMissingValue(val)) {
                vals[i] = Double.MAX_VALUE;
            }
            else {
                vals[i] = val;
            }
        }
        final int[] sortOrder = Utils.sortWithNoMissingValues(vals);
        final Instance[] backup = new Instance[vals.length];
        for (int j = 0; j < vals.length; ++j) {
            backup[j] = this.instance(j);
        }
        for (int j = 0; j < vals.length; ++j) {
            this.m_Instances.setElementAt((Object)backup[sortOrder[j]], j);
        }
    }
    
    public void sort(final Attribute att) {
        this.sort(att.index());
    }
    
    public void stratify(final int numFolds) {
        if (numFolds <= 1) {
            throw new IllegalArgumentException("Number of folds must be greater than 1");
        }
        if (this.m_ClassIndex < 0) {
            throw new UnassignedClassException("Class index is negative (not set)!");
        }
        if (this.classAttribute().isNominal()) {
            for (int index = 1; index < this.numInstances(); ++index) {
                final Instance instance1 = this.instance(index - 1);
                for (int j = index; j < this.numInstances(); ++j) {
                    final Instance instance2 = this.instance(j);
                    if (instance1.classValue() == instance2.classValue() || (instance1.classIsMissing() && instance2.classIsMissing())) {
                        this.swap(index, j);
                        ++index;
                    }
                }
            }
            this.stratStep(numFolds);
        }
    }
    
    public double sumOfWeights() {
        double sum = 0.0;
        for (int i = 0; i < this.numInstances(); ++i) {
            sum += this.instance(i).weight();
        }
        return sum;
    }
    
    public Instances testCV(final int numFolds, final int numFold) {
        if (numFolds < 2) {
            throw new IllegalArgumentException("Number of folds must be at least 2!");
        }
        if (numFolds > this.numInstances()) {
            throw new IllegalArgumentException("Can't have more folds than instances!");
        }
        int numInstForFold = this.numInstances() / numFolds;
        int offset;
        if (numFold < this.numInstances() % numFolds) {
            ++numInstForFold;
            offset = numFold;
        }
        else {
            offset = this.numInstances() % numFolds;
        }
        final Instances test = new Instances(this, numInstForFold);
        final int first = numFold * (this.numInstances() / numFolds) + offset;
        this.copyInstances(first, test, numInstForFold);
        return test;
    }
    
    public String toString() {
        final StringBuffer text = new StringBuffer();
        text.append("@relation").append(" ").append(Utils.quote(this.m_RelationName)).append("\n\n");
        for (int i = 0; i < this.numAttributes(); ++i) {
            text.append(this.attribute(i)).append("\n");
        }
        text.append("\n").append("@data").append("\n");
        text.append(this.stringWithoutHeader());
        return text.toString();
    }
    
    protected String stringWithoutHeader() {
        final StringBuffer text = new StringBuffer();
        for (int i = 0; i < this.numInstances(); ++i) {
            text.append(this.instance(i));
            if (i < this.numInstances() - 1) {
                text.append('\n');
            }
        }
        return text.toString();
    }
    
    public Instances trainCV(final int numFolds, final int numFold) {
        if (numFolds < 2) {
            throw new IllegalArgumentException("Number of folds must be at least 2!");
        }
        if (numFolds > this.numInstances()) {
            throw new IllegalArgumentException("Can't have more folds than instances!");
        }
        int numInstForFold = this.numInstances() / numFolds;
        int offset;
        if (numFold < this.numInstances() % numFolds) {
            ++numInstForFold;
            offset = numFold;
        }
        else {
            offset = this.numInstances() % numFolds;
        }
        final Instances train = new Instances(this, this.numInstances() - numInstForFold);
        final int first = numFold * (this.numInstances() / numFolds) + offset;
        this.copyInstances(0, train, first);
        this.copyInstances(first + numInstForFold, train, this.numInstances() - first - numInstForFold);
        return train;
    }
    
    public Instances trainCV(final int numFolds, final int numFold, final Random random) {
        final Instances train = this.trainCV(numFolds, numFold);
        train.randomize(random);
        return train;
    }
    
    public double variance(final int attIndex) {
        double sum = 0.0;
        double sumSquared = 0.0;
        double sumOfWeights = 0.0;
        if (!this.attribute(attIndex).isNumeric()) {
            throw new IllegalArgumentException("Can't compute variance because attribute is not numeric!");
        }
        for (int i = 0; i < this.numInstances(); ++i) {
            if (!this.instance(i).isMissing(attIndex)) {
                sum += this.instance(i).weight() * this.instance(i).value(attIndex);
                sumSquared += this.instance(i).weight() * this.instance(i).value(attIndex) * this.instance(i).value(attIndex);
                sumOfWeights += this.instance(i).weight();
            }
        }
        if (sumOfWeights <= 1.0) {
            return 0.0;
        }
        final double result = (sumSquared - sum * sum / sumOfWeights) / (sumOfWeights - 1.0);
        if (result < 0.0) {
            return 0.0;
        }
        return result;
    }
    
    public double variance(final Attribute att) {
        return this.variance(att.index());
    }
    
    public AttributeStats attributeStats(final int index) {
        final AttributeStats result = new AttributeStats();
        if (this.attribute(index).isNominal()) {
            result.nominalCounts = new int[this.attribute(index).numValues()];
        }
        if (this.attribute(index).isNumeric()) {
            result.numericStats = new Stats();
        }
        result.totalCount = this.numInstances();
        final double[] attVals = this.attributeToDoubleArray(index);
        final int[] sorted = Utils.sort(attVals);
        int currentCount = 0;
        double prev = Instance.missingValue();
        for (int j = 0; j < this.numInstances(); ++j) {
            final Instance current = this.instance(sorted[j]);
            if (current.isMissing(index)) {
                result.missingCount = this.numInstances() - j;
                break;
            }
            if (current.value(index) == prev) {
                ++currentCount;
            }
            else {
                result.addDistinct(prev, currentCount);
                currentCount = 1;
                prev = current.value(index);
            }
        }
        result.addDistinct(prev, currentCount);
        final AttributeStats attributeStats = result;
        --attributeStats.distinctCount;
        return result;
    }
    
    public double[] attributeToDoubleArray(final int index) {
        final double[] result = new double[this.numInstances()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = this.instance(i).value(index);
        }
        return result;
    }
    
    public String toSummaryString() {
        final StringBuffer result = new StringBuffer();
        result.append("Relation Name:  ").append(this.relationName()).append('\n');
        result.append("Num Instances:  ").append(this.numInstances()).append('\n');
        result.append("Num Attributes: ").append(this.numAttributes()).append('\n');
        result.append('\n');
        result.append(Utils.padLeft("", 5)).append(Utils.padRight("Name", 25));
        result.append(Utils.padLeft("Type", 5)).append(Utils.padLeft("Nom", 5));
        result.append(Utils.padLeft("Int", 5)).append(Utils.padLeft("Real", 5));
        result.append(Utils.padLeft("Missing", 12));
        result.append(Utils.padLeft("Unique", 12));
        result.append(Utils.padLeft("Dist", 6)).append('\n');
        for (int i = 0; i < this.numAttributes(); ++i) {
            final Attribute a = this.attribute(i);
            final AttributeStats as = this.attributeStats(i);
            result.append(Utils.padLeft("" + (i + 1), 4)).append(' ');
            result.append(Utils.padRight(a.name(), 25)).append(' ');
            switch (a.type()) {
                case 1: {
                    result.append(Utils.padLeft("Nom", 4)).append(' ');
                    long percent = Math.round(100.0 * as.intCount / as.totalCount);
                    result.append(Utils.padLeft("" + percent, 3)).append("% ");
                    result.append(Utils.padLeft("0", 3)).append("% ");
                    percent = Math.round(100.0 * as.realCount / as.totalCount);
                    result.append(Utils.padLeft("" + percent, 3)).append("% ");
                    break;
                }
                case 0: {
                    result.append(Utils.padLeft("Num", 4)).append(' ');
                    result.append(Utils.padLeft("0", 3)).append("% ");
                    long percent = Math.round(100.0 * as.intCount / as.totalCount);
                    result.append(Utils.padLeft("" + percent, 3)).append("% ");
                    percent = Math.round(100.0 * as.realCount / as.totalCount);
                    result.append(Utils.padLeft("" + percent, 3)).append("% ");
                    break;
                }
                case 3: {
                    result.append(Utils.padLeft("Dat", 4)).append(' ');
                    result.append(Utils.padLeft("0", 3)).append("% ");
                    long percent = Math.round(100.0 * as.intCount / as.totalCount);
                    result.append(Utils.padLeft("" + percent, 3)).append("% ");
                    percent = Math.round(100.0 * as.realCount / as.totalCount);
                    result.append(Utils.padLeft("" + percent, 3)).append("% ");
                    break;
                }
                case 2: {
                    result.append(Utils.padLeft("Str", 4)).append(' ');
                    long percent = Math.round(100.0 * as.intCount / as.totalCount);
                    result.append(Utils.padLeft("" + percent, 3)).append("% ");
                    result.append(Utils.padLeft("0", 3)).append("% ");
                    percent = Math.round(100.0 * as.realCount / as.totalCount);
                    result.append(Utils.padLeft("" + percent, 3)).append("% ");
                    break;
                }
                case 4: {
                    result.append(Utils.padLeft("Rel", 4)).append(' ');
                    long percent = Math.round(100.0 * as.intCount / as.totalCount);
                    result.append(Utils.padLeft("" + percent, 3)).append("% ");
                    result.append(Utils.padLeft("0", 3)).append("% ");
                    percent = Math.round(100.0 * as.realCount / as.totalCount);
                    result.append(Utils.padLeft("" + percent, 3)).append("% ");
                    break;
                }
                default: {
                    result.append(Utils.padLeft("???", 4)).append(' ');
                    result.append(Utils.padLeft("0", 3)).append("% ");
                    long percent = Math.round(100.0 * as.intCount / as.totalCount);
                    result.append(Utils.padLeft("" + percent, 3)).append("% ");
                    percent = Math.round(100.0 * as.realCount / as.totalCount);
                    result.append(Utils.padLeft("" + percent, 3)).append("% ");
                    break;
                }
            }
            result.append(Utils.padLeft("" + as.missingCount, 5)).append(" /");
            long percent = Math.round(100.0 * as.missingCount / as.totalCount);
            result.append(Utils.padLeft("" + percent, 3)).append("% ");
            result.append(Utils.padLeft("" + as.uniqueCount, 5)).append(" /");
            percent = Math.round(100.0 * as.uniqueCount / as.totalCount);
            result.append(Utils.padLeft("" + percent, 3)).append("% ");
            result.append(Utils.padLeft("" + as.distinctCount, 5)).append(' ');
            result.append('\n');
        }
        return result.toString();
    }
    
    protected void copyInstances(final int from, final Instances dest, final int num) {
        for (int i = 0; i < num; ++i) {
            dest.add(this.instance(from + i));
        }
    }
    
    protected void freshAttributeInfo() {
        this.m_Attributes = (FastVector)this.m_Attributes.copyElements();
    }
    
    protected String instancesAndWeights() {
        final StringBuffer text = new StringBuffer();
        for (int i = 0; i < this.numInstances(); ++i) {
            text.append(this.instance(i) + " " + this.instance(i).weight());
            if (i < this.numInstances() - 1) {
                text.append("\n");
            }
        }
        return text.toString();
    }
    
    protected void stratStep(final int numFolds) {
        final FastVector newVec = new FastVector(this.m_Instances.capacity());
        int start = 0;
        while (newVec.size() < this.numInstances()) {
            for (int j = start; j < this.numInstances(); j += numFolds) {
                newVec.addElement((Object)this.instance(j));
            }
            ++start;
        }
        this.m_Instances = newVec;
    }
    
    public void swap(final int i, final int j) {
        this.m_Instances.swap(i, j);
    }
    
    public static Instances mergeInstances(final Instances first, final Instances second) {
        if (first.numInstances() != second.numInstances()) {
            throw new IllegalArgumentException("Instance sets must be of the same size");
        }
        final FastVector newAttributes = new FastVector();
        for (int i = 0; i < first.numAttributes(); ++i) {
            newAttributes.addElement((Object)first.attribute(i));
        }
        for (int i = 0; i < second.numAttributes(); ++i) {
            newAttributes.addElement((Object)second.attribute(i));
        }
        final Instances merged = new Instances(first.relationName() + '_' + second.relationName(), newAttributes, first.numInstances());
        for (int j = 0; j < first.numInstances(); ++j) {
            merged.add(first.instance(j).mergeInstance(second.instance(j)));
        }
        return merged;
    }
    
    public static void test(final String[] argv) {
        final Random random = new Random(2L);
        try {
            if (argv.length > 1) {
                throw new Exception("Usage: Instances [<filename>]");
            }
            final FastVector testVals = new FastVector(2);
            testVals.addElement((Object)"first_value");
            testVals.addElement((Object)"second_value");
            final FastVector testAtts = new FastVector(2);
            testAtts.addElement((Object)new Attribute("nominal_attribute", testVals));
            testAtts.addElement((Object)new Attribute("numeric_attribute"));
            Instances instances = new Instances("test_set", testAtts, 10);
            instances.add(new Instance(instances.numAttributes()));
            instances.add(new Instance(instances.numAttributes()));
            instances.add(new Instance(instances.numAttributes()));
            instances.setClassIndex(0);
            System.out.println("\nSet of instances created from scratch:\n");
            System.out.println(instances);
            if (argv.length == 1) {
                final String filename = argv[0];
                Reader reader = new FileReader(filename);
                System.out.println("\nFirst five instances from file:\n");
                instances = new Instances(reader, 1);
                instances.setClassIndex(instances.numAttributes() - 1);
                for (int i = 0; i < 5 && instances.readInstance(reader); ++i) {}
                System.out.println(instances);
                reader = new FileReader(filename);
                instances = new Instances(reader);
                instances.setClassIndex(instances.numAttributes() - 1);
                System.out.println("\nDataset:\n");
                System.out.println(instances);
                System.out.println("\nClass index: " + instances.classIndex());
            }
            System.out.println("\nClass name: " + instances.classAttribute().name());
            System.out.println("\nClass index: " + instances.classIndex());
            System.out.println("\nClass is nominal: " + instances.classAttribute().isNominal());
            System.out.println("\nClass is numeric: " + instances.classAttribute().isNumeric());
            System.out.println("\nClasses:\n");
            for (int i = 0; i < instances.numClasses(); ++i) {
                System.out.println(instances.classAttribute().value(i));
            }
            System.out.println("\nClass values and labels of instances:\n");
            for (int i = 0; i < instances.numInstances(); ++i) {
                final Instance inst = instances.instance(i);
                System.out.print(inst.classValue() + "\t");
                System.out.print(inst.toString(inst.classIndex()));
                if (instances.instance(i).classIsMissing()) {
                    System.out.println("\tis missing");
                }
                else {
                    System.out.println();
                }
            }
            System.out.println("\nCreating random weights for instances.");
            for (int i = 0; i < instances.numInstances(); ++i) {
                instances.instance(i).setWeight(random.nextDouble());
            }
            System.out.println("\nInstances and their weights:\n");
            System.out.println(instances.instancesAndWeights());
            System.out.print("\nSum of weights: ");
            System.out.println(instances.sumOfWeights());
            Instances secondInstances = new Instances(instances);
            final Attribute testAtt = new Attribute("Inserted");
            secondInstances.insertAttributeAt(testAtt, 0);
            System.out.println("\nSet with inserted attribute:\n");
            System.out.println(secondInstances);
            System.out.println("\nClass name: " + secondInstances.classAttribute().name());
            secondInstances.deleteAttributeAt(0);
            System.out.println("\nSet with attribute deleted:\n");
            System.out.println(secondInstances);
            System.out.println("\nClass name: " + secondInstances.classAttribute().name());
            System.out.println("\nHeaders equal: " + instances.equalHeaders(secondInstances) + "\n");
            System.out.println("\nData (internal values):\n");
            for (int i = 0; i < instances.numInstances(); ++i) {
                for (int j = 0; j < instances.numAttributes(); ++j) {
                    if (instances.instance(i).isMissing(j)) {
                        System.out.print("? ");
                    }
                    else {
                        System.out.print(instances.instance(i).value(j) + " ");
                    }
                }
                System.out.println();
            }
            System.out.println("\nEmpty dataset:\n");
            final Instances empty = new Instances(instances, 0);
            System.out.println(empty);
            System.out.println("\nClass name: " + empty.classAttribute().name());
            if (empty.classAttribute().isNominal()) {
                final Instances copy = new Instances(empty, 0);
                copy.renameAttribute(copy.classAttribute(), "new_name");
                copy.renameAttributeValue(copy.classAttribute(), copy.classAttribute().value(0), "new_val_name");
                System.out.println("\nDataset with names changed:\n" + copy);
                System.out.println("\nOriginal dataset:\n" + empty);
            }
            final int start = instances.numInstances() / 4;
            final int num = instances.numInstances() / 2;
            System.out.print("\nSubset of dataset: ");
            System.out.println(num + " instances from " + (start + 1) + ". instance");
            secondInstances = new Instances(instances, start, num);
            System.out.println("\nClass name: " + secondInstances.classAttribute().name());
            System.out.println("\nInstances and their weights:\n");
            System.out.println(secondInstances.instancesAndWeights());
            System.out.print("\nSum of weights: ");
            System.out.println(secondInstances.sumOfWeights());
            System.out.println("\nTrain and test folds for 3-fold CV:");
            if (instances.classAttribute().isNominal()) {
                instances.stratify(3);
            }
            for (int j = 0; j < 3; ++j) {
                final Instances train = instances.trainCV(3, j, new Random(1L));
                final Instances test = instances.testCV(3, j);
                System.out.println("\nTrain: ");
                System.out.println("\nInstances and their weights:\n");
                System.out.println(train.instancesAndWeights());
                System.out.print("\nSum of weights: ");
                System.out.println(train.sumOfWeights());
                System.out.println("\nClass name: " + train.classAttribute().name());
                System.out.println("\nTest: ");
                System.out.println("\nInstances and their weights:\n");
                System.out.println(test.instancesAndWeights());
                System.out.print("\nSum of weights: ");
                System.out.println(test.sumOfWeights());
                System.out.println("\nClass name: " + test.classAttribute().name());
            }
            System.out.println("\nRandomized dataset:");
            instances.randomize(random);
            System.out.println("\nInstances and their weights:\n");
            System.out.println(instances.instancesAndWeights());
            System.out.print("\nSum of weights: ");
            System.out.println(instances.sumOfWeights());
            System.out.print("\nInstances sorted according to first attribute:\n ");
            instances.sort(0);
            System.out.println("\nInstances and their weights:\n");
            System.out.println(instances.instancesAndWeights());
            System.out.print("\nSum of weights: ");
            System.out.println(instances.sumOfWeights());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(final String[] args) {
        try {
            if (args.length == 0) {
                final ConverterUtils.DataSource source = new ConverterUtils.DataSource(System.in);
                final Instances i = source.getDataSet();
                System.out.println(i.toSummaryString());
            }
            else if (args.length == 1 && !args[0].equals("-h") && !args[0].equals("help")) {
                final ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[0]);
                final Instances i = source.getDataSet();
                System.out.println(i.toSummaryString());
            }
            else if (args.length == 3 && args[0].toLowerCase().equals("merge")) {
                final ConverterUtils.DataSource source2 = new ConverterUtils.DataSource(args[1]);
                final ConverterUtils.DataSource source3 = new ConverterUtils.DataSource(args[2]);
                final Instances i = mergeInstances(source2.getDataSet(), source3.getDataSet());
                System.out.println(i);
            }
            else if (args.length == 3 && args[0].toLowerCase().equals("append")) {
                final ConverterUtils.DataSource source2 = new ConverterUtils.DataSource(args[1]);
                final ConverterUtils.DataSource source3 = new ConverterUtils.DataSource(args[2]);
                if (!source2.getStructure().equalHeaders(source3.getStructure())) {
                    throw new Exception("The two datasets have different headers!");
                }
                Instances structure = source2.getStructure();
                System.out.println(source2.getStructure());
                while (source2.hasMoreElements(structure)) {
                    System.out.println(source2.nextElement(structure));
                }
                structure = source3.getStructure();
                while (source3.hasMoreElements(structure)) {
                    System.out.println(source3.nextElement(structure));
                }
            }
            else if (args.length == 3 && args[0].toLowerCase().equals("headers")) {
                final ConverterUtils.DataSource source2 = new ConverterUtils.DataSource(args[1]);
                final ConverterUtils.DataSource source3 = new ConverterUtils.DataSource(args[2]);
                if (source2.getStructure().equalHeaders(source3.getStructure())) {
                    System.out.println("Headers match");
                }
                else {
                    System.out.println("Headers don't match");
                }
            }
            else if (args.length == 3 && args[0].toLowerCase().equals("randomize")) {
                final ConverterUtils.DataSource source = new ConverterUtils.DataSource(args[2]);
                final Instances i = source.getDataSet();
                i.randomize(new Random(Integer.parseInt(args[1])));
                System.out.println(i);
            }
            else {
                System.err.println("\nUsage:\n\tweka.core.Instances help\n\tweka.core.Instances <filename>\n\tweka.core.Instances merge <filename1> <filename2>\n\tweka.core.Instances append <filename1> <filename2>\n\tweka.core.Instances headers <filename1> <filename2>\n\tweka.core.Instances randomize <seed> <filename>\n");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
        }
    }
    
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 10497 $");
    }
}
