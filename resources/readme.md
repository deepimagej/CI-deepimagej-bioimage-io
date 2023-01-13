# Table with status of models.
Local backup.

## link google spreadsheet
https://docs.google.com/spreadsheets/d/1AsTPSlum3HH_11_SxEKFdjoWt9-1Xx542pZSxicDx-g/edit#gid=0

# Shapes of numpy test inputs for use-cases
- important becaues this is the tile-size argument for testing in deepimagej
- easy to do in python, impossible or very hard to do in java

````python
a = np.load('/home/deck/Fiji.app/models/Cell Segmentation from Membrane Staining for Plant Tissues_07012023_223258/exampleImage.npy')
a.shape
(1, 256, 256, 8, 1)

b = np.load('/home/deck/Fiji.app/models/StarDist H&E Nuclei Segmentation_13012023_211932/test_input.npy')
b.shape
(1, 304, 512, 3)

c=np.load('/home/deck/Fiji.app/models/EnhancerMitochondriaEM2D_13012023_213240/test_input_0.npy')
c.shape
(1, 1, 512, 512)

d=np.load('/home/deck/Fiji.app/models/HPA Nucleus Segmentation (DPNUnet)_13012023_213325/test_nuclei_input.npy')
d.shape
(1, 3, 512, 512)

````
