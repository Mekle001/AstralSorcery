package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.registry.RegistryItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockMarble
 * Created by HellFirePvP
 * Date: 22.05.2016 / 16:13
 */
public class BlockMarble extends Block implements BlockCustomName, BlockVariants {

    //private static final int RAND_MOSS_CHANCE = 10;

    public static PropertyEnum<MarbleBlockType> MARBLE_TYPE = PropertyEnum.create("marbletype", MarbleBlockType.class);

    public BlockMarble() {
        super(Material.ROCK, MapColor.GRAY);
        setHardness(1.0F);
        setHarvestLevel("pickaxe", 2);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);
        //setTickRandomly(true);
        setCreativeTab(RegistryItems.creativeTabAstralSorcery);
        setDefaultState(this.blockState.getBaseState().withProperty(MARBLE_TYPE, MarbleBlockType.RAW));
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (MarbleBlockType t : MarbleBlockType.values()) {
            if(!t.obtainableInCreative()) continue;
            list.add(new ItemStack(item, 1, t.ordinal()));
        }
    }

    /*@Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote && worldIn.isRaining() && rand.nextInt(RAND_MOSS_CHANCE) == 0) {
            MarbleBlockType type = state.getValue(MARBLE_TYPE);
            if (type.canTurnMossy() && worldIn.isRainingAt(pos)) {
                worldIn.setBlockState(pos, state.withProperty(MARBLE_TYPE, type.getMossyEquivalent()), 3);
            }
        }
    }*/

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        //return super.getActualState(state, worldIn, pos);
        if(state.getValue(MARBLE_TYPE).isPillar()) {
            IBlockState st = worldIn.getBlockState(pos.up());
            boolean top = false;
            if(st.getBlock() instanceof BlockMarble && st.getValue(MARBLE_TYPE).isPillar()) {
                top = true;
            }
            st = worldIn.getBlockState(pos.down());
            boolean down = false;
            if(st.getBlock() instanceof BlockMarble && st.getValue(MARBLE_TYPE).isPillar()) {
                down = true;
            }
            if(top && down) {
                return state.withProperty(MARBLE_TYPE, MarbleBlockType.PILLAR);
            } else if(top) {
                return state.withProperty(MARBLE_TYPE, MarbleBlockType.PILLAR_BOTTOM);
            } else if(down) {
                return state.withProperty(MARBLE_TYPE, MarbleBlockType.PILLAR_TOP);
            } else {
                return state.withProperty(MARBLE_TYPE, MarbleBlockType.PILLAR);
            }
        }
        return super.getActualState(state, worldIn, pos);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return true;
    }

    @Override
    public boolean isFullyOpaque(IBlockState state) {
        return true;
    }

    @Override
    public String getIdentifierForMeta(int meta) {
        MarbleBlockType mt = getStateFromMeta(meta).getValue(MARBLE_TYPE);
        return mt == null ? "null" : mt.getName();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        MarbleBlockType type = state.getValue(MARBLE_TYPE);
        return type == null ? 0 : type.getMeta();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return meta < MarbleBlockType.values().length ? getDefaultState().withProperty(MARBLE_TYPE, MarbleBlockType.values()[meta]) : getDefaultState();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MARBLE_TYPE);
    }

    @Override
    public List<IBlockState> getValidStates() {
        List<IBlockState> ret = new LinkedList<>();
        for (MarbleBlockType type : MarbleBlockType.values()) {
            ret.add(getDefaultState().withProperty(MARBLE_TYPE, type));
        }
        return ret;
    }

    @Override
    public String getStateName(IBlockState state) {
        return state.getValue(MARBLE_TYPE).getName();
    }

    public static enum MarbleBlockType implements IStringSerializable {

        RAW(0),
        BRICKS(1),
        PILLAR(2),
        ARCH(3),
        CHISELED(4),
        ENGRAVED(5),
        RUNED(6),

        PILLAR_TOP(2),
        PILLAR_BOTTOM(2);

        //BRICKS_MOSSY,
        //PILLAR_MOSSY,
        //CRACK_MOSSY;

        private final int meta;

        private MarbleBlockType(int meta) {
            this.meta = meta;
        }

        public ItemStack asStack() {
            return new ItemStack(BlocksAS.blockMarble, 1, meta);
        }

        public boolean isPillar() {
            return this == PILLAR_BOTTOM || this == PILLAR || this == PILLAR_TOP;
        }

        public boolean obtainableInCreative() {
            return this != PILLAR_TOP && this != PILLAR_BOTTOM;
        }

        public int getMeta() {
            return meta;
        }

        @Override
        public String getName() {
            return name().toLowerCase();
        }

        /*public boolean canTurnMossy() {
            return this == BRICKS || this == PILLAR || this == CRACKED;
        }

        public MarbleBlockType getMossyEquivalent() {
            if(!canTurnMossy()) return null;
            switch (this) {
                case BRICKS:
                    return BRICKS_MOSSY;
                case PILLAR:
                    return PILLAR_MOSSY;
                case CRACKED:
                    return CRACK_MOSSY;
            }
            return null;
        }*/
    }

}
