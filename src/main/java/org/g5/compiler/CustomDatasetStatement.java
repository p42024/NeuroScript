package org.g5.compiler;

public class CustomDatasetStatement extends Statement {
    private static final String dataset = """
            class CustomDataset(Dataset):
                def __init__(self, csv_file, target_column):
                    self.data_frame = pd.read_csv(csv_file)
                    self.target_column = target_column
                            
                def __len__(self):
                    return len(self.data_frame)
                            
                def __getitem__(self, idx):
                    if torch.is_tensor(idx):
                        idx = idx.tolist()
                            
                    sample = self.data_frame.iloc[idx, :]
                    target = sample[self.target_column]
                    sample = sample.drop(labels=[self.target_column])
                            
                    sample = torch.tensor(sample.values, dtype=torch.float32)
                    target = torch.tensor(target, dtype=torch.long)
                            
                    return sample, target""";

    public CustomDatasetStatement() {
        super(0, dataset);
    }
}
