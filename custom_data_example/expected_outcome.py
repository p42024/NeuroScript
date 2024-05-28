import torch
import pandas as pd
from torch import nn
from torch.utils.data import DataLoader, Dataset
from torchvision import datasets
from torchvision.transforms import ToTensor
import os

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

        return sample, target
seq_model = nn.Sequential(nn.Linear(3, 10), nn.ReLU(), nn.Linear(10, 3))

def train_0(seq_model):
    custom_data_dataset = CustomDataset(csv_file='custom_data.csv', target_column='target')
    custom_data = DataLoader(custom_data_dataset, batch_size=16, shuffle=True)
    epoch = 0
    CrossEntropy_0 = nn.CrossEntropyLoss()
    optimiser_0 = torch.optim.SGD(seq_model.parameters(), lr=0.01)
    
    while True:
        for data, labels in custom_data:
            train_prediction = seq_model(data)
            loss = CrossEntropy_0(train_prediction, labels)
            loss.backward()
            optimiser_0.step()
            optimiser_0.zero_grad()
            
        print("Loss: ")
        print(loss)
        if (epoch > 10):
            break
        epoch = (epoch + 1)
    
    torch.save(seq_model, os.getcwd() + '\\trained_model.pt')

class seq_model_0(nn.Module):
	def __init__(self):
		super().__init__()
		self.flatten = nn.Flatten()
		self.stack = seq_model

	def forward(self, x):
		if len(x.size()) > 1:
			x = self.flatten(x)
		logits = self.stack(x)
		return logits
train_0(seq_model_0().to("cpu"))
